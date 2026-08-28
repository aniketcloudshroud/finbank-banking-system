package com.finbank.service;

import com.finbank.dto.*;
import com.finbank.entity.Account;
import com.finbank.entity.AccountStatus;
import com.finbank.entity.Transaction;
import com.finbank.entity.TransactionStatus;
import com.finbank.entity.TransactionType;
import com.finbank.exception.AccountNotActiveException;
import com.finbank.exception.AccountNotFoundException;
import com.finbank.exception.InsufficientBalanceException;
import com.finbank.exception.SameAccountTransferException;
import com.finbank.exception.TransactionNotFoundException;
import com.finbank.repository.AccountRepository;
import com.finbank.repository.TransactionRepository;
import jakarta.persistence.criteria.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.*;
import java.util.*;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TransactionResponseDto deposit(
            String accountNumber,
            DepositRequestDto request
    ) {
        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Deposit amount must be greater than zero"
            );
        }


        Long customerId =
                currentUserService.getCurrentCustomerId();

        Account account =
                accountRepository
                        .findByAccountNumberAndCustomerIdForUpdate(
                                accountNumber,
                                customerId
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account number " +
                                                accountNumber +
                                                " not found"
                                )
                        );

        validateActiveAccount(account);

        BigDecimal amount = request.getAmount();

        account.setBalance(
                account.getBalance().add(amount)
        );

        Transaction transaction =
                new Transaction();

        transaction.setReference(
                generateTransactionReference()
        );

        transaction.setType(
                TransactionType.DEPOSIT
        );

        transaction.setAmount(amount);

        transaction.setDescription(
                request.getDescription()
        );

        transaction.setStatus(
                TransactionStatus.SUCCESS
        );

        transaction.setDestinationAccount(account);

        /*
         * Because this method is transactional, JPA will persist
         * the balance change when the transaction commits.
         */
        accountRepository.save(account);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return new TransactionResponseDto(
                savedTransaction
        );
    }

    @Transactional
    public TransactionResponseDto withdraw(
            String accountNumber,
            WithdrawalRequestDto request
    ) {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        Account account =
                accountRepository
                        .findByAccountNumberAndCustomerIdForUpdate(
                                accountNumber,
                                customerId
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account number " +
                                                accountNumber +
                                                " not found"
                                )
                        );

        validateActiveAccount(account);

        BigDecimal amount = request.getAmount();

        validateSufficientBalance(
                account,
                amount
        );

        account.setBalance(
                account.getBalance().subtract(amount)
        );

        Transaction transaction =
                new Transaction();

        transaction.setReference(
                generateTransactionReference()
        );

        transaction.setType(
                TransactionType.WITHDRAWAL
        );

        transaction.setAmount(amount);

        transaction.setDescription(
                request.getDescription()
        );

        transaction.setStatus(
                TransactionStatus.SUCCESS
        );

        transaction.setSourceAccount(account);

        accountRepository.save(account);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return new TransactionResponseDto(
                savedTransaction
        );
    }

    @Transactional
    public TransactionResponseDto transfer(
            String sourceAccountNumber,
            TransferRequestDto request
    ) {

        String destinationAccountNumber =
                request.getDestinationAccountNumber()
                        .trim();

        if (sourceAccountNumber.equals(
                destinationAccountNumber
        )) {

            throw new SameAccountTransferException(
                    "Source and destination account numbers are the same"
            );
        }

        Long customerId =
                currentUserService.getCurrentCustomerId();

        Account sourceAccount =
                accountRepository
                        .findByAccountNumberAndCustomerIdForUpdate(
                                sourceAccountNumber,
                                customerId
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account number " +
                                                sourceAccountNumber +
                                                " not found"
                                )
                        );

        Account destinationAccount =
                accountRepository
                        .findByAccountNumber(
                                destinationAccountNumber
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account number " +
                                                destinationAccountNumber +
                                                " not found"
                                )
                        );

        validateActiveAccount(sourceAccount);
        validateActiveAccount(destinationAccount);

        BigDecimal amount =
                request.getAmount();

        validateSufficientBalance(
                sourceAccount,
                amount
        );

        sourceAccount.setBalance(
                sourceAccount.getBalance()
                        .subtract(amount)
        );

        destinationAccount.setBalance(
                destinationAccount.getBalance()
                        .add(amount)
        );

        Transaction transaction =
                new Transaction();

        transaction.setReference(
                generateTransactionReference()
        );

        transaction.setType(
                TransactionType.TRANSFER
        );

        transaction.setAmount(amount);

        transaction.setDescription(
                request.getDescription()
        );

        transaction.setStatus(
                TransactionStatus.SUCCESS
        );

        transaction.setSourceAccount(
                sourceAccount
        );

        transaction.setDestinationAccount(
                destinationAccount
        );

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return new TransactionResponseDto(
                savedTransaction
        );
    }

    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionByReference(
            String reference
    ) {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        Transaction transaction =
                transactionRepository
                        .findByReference(reference)
                        .orElseThrow(() ->
                                new TransactionNotFoundException(
                                        "Transaction with reference " +
                                                reference +
                                                " not found"
                                )
                        );

        boolean sourceBelongsToCustomer =
                transaction.getSourceAccount() != null
                        && transaction.getSourceAccount()
                        .getCustomer()
                        .getId()
                        .equals(customerId);

        boolean destinationBelongsToCustomer =
                transaction.getDestinationAccount() != null
                        && transaction.getDestinationAccount()
                        .getCustomer()
                        .getId()
                        .equals(customerId);

        if (!sourceBelongsToCustomer &&
                !destinationBelongsToCustomer) {

            throw new TransactionNotFoundException(
                    "Transaction with reference " +
                            reference +
                            " not found"
            );
        }

        return new TransactionResponseDto(
                transaction
        );
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getAccountTransactions(
            String accountNumber,
            TransactionFilterDto filter,
            Pageable pageable
    ) {

        validateTransactionFilter(filter);

        Long customerId =
                currentUserService.getCurrentCustomerId();

        Account account =
                accountRepository
                        .findByAccountNumberAndCustomerIdForUpdate(
                                accountNumber,
                                customerId
                        )
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        "Account number " +
                                                accountNumber +
                                                " not found"
                                )
                        );

        var specification =
                (org.springframework.data.jpa.domain.Specification<Transaction>)
                        (root, query, cb) -> {

                            List<Predicate> predicates =
                                    new ArrayList<>();

                            Predicate source =
                                    cb.equal(
                                            root.get("sourceAccount")
                                                    .get("accountNumber"),
                                            account.getAccountNumber()
                                    );

                            Predicate destination =
                                    cb.equal(
                                            root.get("destinationAccount")
                                                    .get("accountNumber"),
                                            account.getAccountNumber()
                                    );

                            predicates.add(
                                    cb.or(source, destination)
                            );

                            if (filter != null) {

                                if (filter.getType() != null) {

                                    predicates.add(
                                            cb.equal(
                                                    root.get("type"),
                                                    filter.getType()
                                            )
                                    );
                                }

                                if (filter.getFromDate() != null) {

                                    predicates.add(
                                            cb.greaterThanOrEqualTo(
                                                    root.get("createdAt"),
                                                    filter.getFromDate()
                                                            .atStartOfDay()
                                            )
                                    );
                                }

                                if (filter.getToDate() != null) {

                                    LocalDateTime endOfDay =
                                            filter.getToDate()
                                                    .plusDays(1)
                                                    .atStartOfDay();

                                    predicates.add(
                                            cb.lessThan(
                                                    root.get("createdAt"),
                                                    endOfDay
                                            )
                                    );
                                }

                                if (filter.getMinAmount() != null) {

                                    predicates.add(
                                            cb.greaterThanOrEqualTo(
                                                    root.get("amount"),
                                                    filter.getMinAmount()
                                            )
                                    );
                                }

                                if (filter.getMaxAmount() != null) {

                                    predicates.add(
                                            cb.lessThanOrEqualTo(
                                                    root.get("amount"),
                                                    filter.getMaxAmount()
                                            )
                                    );
                                }

                                if (filter.getSearch() != null &&
                                        !filter.getSearch()
                                                .isBlank()) {

                                    String search =
                                            "%" +
                                                    filter.getSearch()
                                                            .trim()
                                                            .toLowerCase() +
                                                    "%";

                                    predicates.add(
                                            cb.or(
                                                    cb.like(
                                                            cb.lower(
                                                                    root.get(
                                                                            "description"
                                                                    )
                                                            ),
                                                            search
                                                    ),
                                                    cb.like(
                                                            cb.lower(
                                                                    root.get(
                                                                            "reference"
                                                                    )
                                                            ),
                                                            search
                                                    )
                                            )
                                    );
                                }
                            }

                            return cb.and(
                                    predicates.toArray(
                                            new Predicate[0]
                                    )
                            );
                        };

        return transactionRepository
                .findAll(specification, pageable)
                .map(TransactionResponseDto::new);
    }

    private Account getCustomerAccount(
            String accountNumber,
            Long customerId
    ) {

        return accountRepository
                .findByAccountNumberAndCustomerId(
                        accountNumber,
                        customerId
                )
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account number " +
                                        accountNumber +
                                        " not found"
                        )
                );
    }

    private void validateActiveAccount(
            Account account
    ) {

        if (account.getStatus() != AccountStatus.ACTIVE) {

            throw new AccountNotActiveException(
                    "Account " +
                            account.getAccountNumber() +
                            " is not active and cannot perform this operation"
            );
        }
    }

    private void validateSufficientBalance(
            Account account,
            BigDecimal amount
    ) {

        if (account.getBalance()
                .compareTo(amount) < 0) {

            throw new InsufficientBalanceException(
                    "Insufficient balance in account " +
                            account.getAccountNumber()
            );
        }
    }

    private String generateTransactionReference() {

        String reference;

        do {

            long number =
                    100_000_000L
                            + secureRandom.nextLong(
                            900_000_000L
                    );

            reference = "TXN-" + number;

        } while (
                transactionRepository
                        .existsByReference(reference)
        );

        return reference;
    }

    private void validateTransactionFilter(
            TransactionFilterDto filter
    ) {

        if (filter == null) {
            return;
        }

        if (filter.getFromDate() != null &&
                filter.getToDate() != null &&
                filter.getFromDate()
                        .isAfter(filter.getToDate())) {

            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        if (filter.getMinAmount() != null &&
                filter.getMaxAmount() != null &&
                filter.getMinAmount()
                        .compareTo(filter.getMaxAmount()) > 0) {

            throw new IllegalArgumentException(
                    "Minimum amount cannot be greater than maximum amount"
            );
        }

        if (filter.getMinAmount() != null &&
                filter.getMinAmount().signum() < 0) {

            throw new IllegalArgumentException(
                    "Minimum amount cannot be negative"
            );
        }

        if (filter.getMaxAmount() != null &&
                filter.getMaxAmount().signum() < 0) {

            throw new IllegalArgumentException(
                    "Maximum amount cannot be negative"
            );
        }
    }
}