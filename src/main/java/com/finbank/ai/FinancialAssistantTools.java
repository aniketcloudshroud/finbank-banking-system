package com.finbank.ai;

import com.finbank.dto.AccountResponseDto;
import com.finbank.dto.TransactionResponseDto;
import com.finbank.entity.Transaction;
import com.finbank.repository.AccountRepository;
import com.finbank.repository.TransactionRepository;
import com.finbank.service.CurrentUserService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FinancialAssistantTools {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public FinancialAssistantTools(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            CurrentUserService currentUserService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Tool(
            name = "getMyAccounts",
            description = """
                    Retrieve the currently authenticated customer's bank accounts.
                    Use this whenever the user asks about their accounts, balances,
                    account types, account status, or total money across their accounts.
                    Never use this tool to retrieve another customer's accounts.
                    """
    )
    public List<AccountResponseDto> getMyAccounts() {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        return accountRepository
                .findByCustomerId(
                        customerId,
                        PageRequest.of(
                                0,
                                50,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "createdAt"
                                )
                        )
                )
                .getContent()
                .stream()
                .map(AccountResponseDto::new)
                .toList();
    }

    @Tool(
            name = "getMyRecentTransactions",
            description = """
                    Retrieve recent transactions belonging to the currently
                    authenticated customer. Use this when the user asks about
                    recent transactions, spending, deposits, withdrawals,
                    transfers, transaction history, or financial activity.
                    Never retrieve another customer's transactions.
                    """
    )
    public List<TransactionResponseDto> getMyRecentTransactions(
            @ToolParam(
                    description = "Maximum number of recent transactions to return. Use a value between 1 and 50."
            )
            int limit
    ) {

        int safeLimit =
                Math.max(
                        1,
                        Math.min(limit, 50)
                );

        Long customerId =
                currentUserService.getCurrentCustomerId();

        return transactionRepository
                .findBySourceAccountCustomerIdOrDestinationAccountCustomerId(
                        customerId,
                        customerId,
                        PageRequest.of(
                                0,
                                safeLimit,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "createdAt"
                                )
                        )
                )
                .getContent()
                .stream()
                .map(TransactionResponseDto::new)
                .toList();
    }

    @Tool(
            name = "getMyAccount",
            description = """
                    Retrieve details of one account belonging to the currently
                    authenticated customer. Use this only when the user provides
                    an account number or clearly refers to a specific account.
                    The account number must belong to the authenticated customer.
                    """
    )
    public AccountResponseDto getMyAccount(
            @ToolParam(
                    description = "The account number belonging to the currently authenticated customer."
            )
            String accountNumber
    ) {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        return accountRepository
                .findByAccountNumberAndCustomerId(
                        accountNumber,
                        customerId
                )
                .map(AccountResponseDto::new)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Account not found"
                        )
                );
    }

    @Tool(
            name = "getMyTransaction",
            description = """
                    Retrieve a transaction by reference, but only if the
                    transaction belongs to the currently authenticated customer.
                    Use this when the user asks about a specific transaction
                    reference.
                    """
    )
    public TransactionResponseDto getMyTransaction(
            @ToolParam(
                    description = "The transaction reference belonging to the current customer."
            )
            String reference
    ) {

        Long customerId =
                currentUserService.getCurrentCustomerId();

        Transaction transaction =
                transactionRepository
                        .findByReference(reference)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Transaction not found"
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

            throw new IllegalArgumentException(
                    "Transaction not found"
            );
        }

        return new TransactionResponseDto(transaction);
    }
}