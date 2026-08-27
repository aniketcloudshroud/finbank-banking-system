package com.finbank.service;

import com.finbank.dto.*;
import com.finbank.entity.*;
import com.finbank.entity.User;
import com.finbank.exception.*;
import com.finbank.repository.*;
import org.springframework.data.domain.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.*;

import java.security.*;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          UserRepository userRepository,
                          CurrentUserService currentUserService) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public AccountResponseDto getAccountByAccountNumber(String accountNumber) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User with email " + email + " not found"
                        )
                );

        Long customerId = user.getCustomer().getId();

        Account account = accountRepository
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

    public AccountResponseDto createAccount(
            Long customerId,
            AccountRequestDto requestDto) {

        Long authenticatedCustomerId =
                currentUserService.getCurrentCustomerId();

        if (!authenticatedCustomerId.equals(customerId)) {
            throw new CustomerNotFoundException(
                    "Customer with id " + customerId + " not found"
            );
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new CustomerNotFoundException(
                                "Customer with id " +
                                        customerId +
                                        " not found"
                        )
                );

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(requestDto.getAccountType());
        account.setCurrency(requestDto.getCurrency());
        account.setCustomer(customer);

        Account savedAccount = accountRepository.save(account);

        return new AccountResponseDto(savedAccount);
    }

    private String generateAccountNumber() {
        String accountNumber;

        do {
            long number = 1_000_000_000L
                    + secureRandom.nextLong(9_000_000_000L);
            accountNumber = "FIN" + number;
        } while(accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    public Page<AccountResponseDto> getAllAccounts(Pageable pageable) {

        return accountRepository.findAll(pageable)
                .map(AccountResponseDto::new);
    }

}
