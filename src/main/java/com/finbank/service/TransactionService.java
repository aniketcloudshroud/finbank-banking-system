package com.finbank.service;


import com.finbank.dto.*;
import com.finbank.entity.*;
import com.finbank.entity.Transaction;
import com.finbank.exception.*;
import com.finbank.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

import java.security.*;

@Service
public class TransactionService {

    private AccountRepository accountRepository;
    private TransactionRepository transactionRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final CurrentUserService currentUserService;

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
        Long customerId = currentUserService.getCurrentCustomerId();

        Account toAccount = accountRepository
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

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account " + accountNumber +
                            " is not active and cannot perform this operation"
            );
        }


        toAccount.setBalance(
                toAccount.getBalance().add(request.getAmount())
        );

        Transaction transaction = new Transaction();

        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDestinationAccount(toAccount);
        transaction.setReference(generateTransactionReference());

        accountRepository.save(toAccount);

        Transaction newTransaction =
                transactionRepository.save(transaction);

        return new TransactionResponseDto(newTransaction);
    }

    private String generateTransactionReference() {
        String reference;

        do {
            long number = 100_000_000L
                    + secureRandom.nextLong(900_000_000L);

            reference = "TXN - " + number;
        } while(transactionRepository.existsByReference(reference));

        return reference;
    }

    @Transactional
    public TransactionResponseDto withdraw(
            String accountNumber,
            WithdrawalRequestDto request
    ) {

        Long customerId = currentUserService.getCurrentCustomerId();

        Account fromAccount = accountRepository
                .findByAccountNumberAndCustomerId(
                        accountNumber,
                        customerId
                )
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account number " +
                                        accountNumber +
                                        " not found"
                        ));

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account " + accountNumber +
                            " is not active and cannot perform this operation"
            );
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + accountNumber
            );
        }

        fromAccount.setBalance(
                fromAccount.getBalance().subtract(request.getAmount())
        );

        Transaction transaction = new Transaction();

        transaction.setReference(generateTransactionReference());
        transaction.setType(TransactionType.WITHDRAWAL);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setSourceAccount(fromAccount);

        accountRepository.save(fromAccount);

        Transaction newTransaction =
                transactionRepository.save(transaction);

        return new TransactionResponseDto(newTransaction);
    }

    @Transactional
    public TransactionResponseDto transfer(
            String sourceAccountNumber,
            TransferRequestDto request) {

        String toAccountNumber = request.getDestinationAccountNumber();
        if (sourceAccountNumber.equals(toAccountNumber)) throw new SameAccountTransferException(
                "Source and Destination Account number are same");

        Long customerId = currentUserService.getCurrentCustomerId();

        Account fromAccount = accountRepository
                .findByAccountNumberAndCustomerId(
                        sourceAccountNumber,
                        customerId
                )
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account number " +
                                        sourceAccountNumber +
                                        " not found"
                        ));

        Account toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account number " + toAccountNumber + " not found"
                        ));

        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account " + sourceAccountNumber +
                            " is not active and cannot perform this operation"
            );
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account " + toAccountNumber +
                            " is not active and cannot perform this operation"
            );
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + sourceAccountNumber
            );
        }

        fromAccount.setBalance(
                fromAccount.getBalance().subtract(request.getAmount())
        );

        toAccount.setBalance(
                toAccount.getBalance().add(request.getAmount())
        );

        Transaction transaction = new Transaction();

        transaction.setReference(generateTransactionReference());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setSourceAccount(fromAccount);
        transaction.setDestinationAccount(toAccount);

        Transaction finalTransaction = transactionRepository.save(transaction);
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return new TransactionResponseDto(finalTransaction);
    }



    @Transactional(readOnly = true)
    public TransactionResponseDto getTransactionByReference(String reference) {

        Long customerId = currentUserService.getCurrentCustomerId();

        Transaction transaction = transactionRepository
                .findByReference(reference)
                .orElseThrow(() ->
                        new TransactionNotFoundException(
                                "Transaction with reference " +
                                        reference +
                                        " not found"
                        ));

        boolean sourceBelongsToCustomer =
                transaction.getSourceAccount() != null &&
                        transaction.getSourceAccount()
                                .getCustomer()
                                .getId()
                                .equals(customerId);

        boolean destinationBelongsToCustomer =
                transaction.getDestinationAccount() != null &&
                        transaction.getDestinationAccount()
                                .getCustomer()
                                .getId()
                                .equals(customerId);

        if (!sourceBelongsToCustomer && !destinationBelongsToCustomer) {
            throw new TransactionNotFoundException(
                    "Transaction with reference " +
                            reference +
                            " not found"
            );
        }

        return new TransactionResponseDto(transaction);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponseDto> getAccountTransactions(
            String accountNumber,
            Pageable pageable
    ) {
        Long customerId = currentUserService.getCurrentCustomerId();

        Account account = accountRepository
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

        Page<Transaction> transactions =
                transactionRepository
                        .findBySourceAccountAccountNumberOrDestinationAccountAccountNumber(
                                account.getAccountNumber(),
                                account.getAccountNumber(),
                                pageable
                        );

        return transactions.map(TransactionResponseDto::new);
    }
}
