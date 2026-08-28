package com.finbank.controller;

import com.finbank.dto.DepositRequestDto;
import com.finbank.dto.TransactionFilterDto;
import com.finbank.dto.TransactionResponseDto;
import com.finbank.dto.TransferRequestDto;
import com.finbank.dto.WithdrawalRequestDto;
import com.finbank.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasRole('CUSTOMER')")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService
    ) {
        this.transactionService = transactionService;
    }

    @PostMapping("/{accountNumber}/deposit")
    public TransactionResponseDto deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequestDto request
    ) {

        return transactionService.deposit(
                accountNumber,
                request
        );
    }

    @PostMapping("/{accountNumber}/withdraw")
    public TransactionResponseDto withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody WithdrawalRequestDto request
    ) {

        return transactionService.withdraw(
                accountNumber,
                request
        );
    }

    @PostMapping("/{sourceAccountNumber}/transfer")
    public TransactionResponseDto transfer(
            @PathVariable String sourceAccountNumber,
            @Valid @RequestBody TransferRequestDto request
    ) {

        return transactionService.transfer(
                sourceAccountNumber,
                request
        );
    }

    @GetMapping("/{accountNumber}/transactions")
    public Page<TransactionResponseDto> getAccountTransactions(
            @PathVariable String accountNumber,

            @ModelAttribute
            TransactionFilterDto filter,

            @PageableDefault(
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {

        return transactionService.getAccountTransactions(
                accountNumber,
                filter,
                pageable
        );
    }

    @GetMapping("/transactions/{reference}")
    public TransactionResponseDto getTransaction(
            @PathVariable String reference
    ) {

        return transactionService
                .getTransactionByReference(reference);
    }
}