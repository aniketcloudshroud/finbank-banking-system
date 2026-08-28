package com.finbank.service;

import com.finbank.dto.AccountRequestDto;
import com.finbank.dto.AccountResponseDto;
import com.finbank.entity.Account;
import com.finbank.entity.Customer;
import com.finbank.exception.AccountNotFoundException;
import com.finbank.exception.CustomerNotFoundException;
import com.finbank.repository.AccountRepository;
import com.finbank.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final CurrentUserService currentUserService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            CurrentUserService currentUserService
    ) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public AccountResponseDto createAccount(
            Long customerId,
            AccountRequestDto request
    ) {

        Long authenticatedCustomerId =
                currentUserService.getCurrentCustomerId();

        if (!authenticatedCustomerId.equals(customerId)) {
            throw new CustomerNotFoundException(
                    "Customer with id " +
                            customerId +
                            " not found"
            );
        }

        Customer customer =
                customerRepository.findById(customerId)
                        .orElseThrow(() ->
                                new CustomerNotFoundException(
                                        "Customer with id " +
                                                customerId +
                                                " not found"
                                )
                        );

        Account account = new Account();

        account.setAccountNumber(
                generateAccountNumber()
        );

        account.setAccountType(
                request.getAccountType()
        );

        account.setCurrency(
                request.getCurrency()
        );

        account.setCustomer(customer);

        Account savedAccount =
                accountRepository.save(account);

        return new AccountResponseDto(savedAccount);
    }

    @Transactional
    public AccountResponseDto getAccountByAccountNumber(
            String accountNumber
    ) {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        Account account =
                accountRepository
                        .findByAccountNumberAndCustomerId(
                                accountNumber,
                                customerId
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account with number " +
                                                accountNumber +
                                                " not found"
                                )
                        );

        return new AccountResponseDto(account);
    }

    @Transactional
    public Page<AccountResponseDto> getCurrentCustomerAccounts(
            Pageable pageable
    ) {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        return accountRepository
                .findByCustomerId(customerId, pageable)
                .map(AccountResponseDto::new);
    }

    public Page<AccountResponseDto> getAllAccounts(
            Pageable pageable
    ) {

        return accountRepository
                .findAll(pageable)
                .map(AccountResponseDto::new);
    }

    private String generateAccountNumber() {

        String accountNumber;

        do {

            long number =
                    1_000_000_000L
                            + secureRandom.nextLong(
                            9_000_000_000L
                    );

            accountNumber = "FIN" + number;

        } while (
                accountRepository
                        .existsByAccountNumber(accountNumber)
        );

        return accountNumber;
    }
}