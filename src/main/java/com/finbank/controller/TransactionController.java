package com.finbank.controller;


import com.finbank.dto.*;
import com.finbank.service.*;
import jakarta.validation.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class TransactionController {

        private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/{accountNumber}/deposit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionResponseDto deposit(
            @PathVariable String accountNumber,
            @Valid @RequestBody DepositRequestDto request) {

        return transactionService.deposit(accountNumber, request);
    }

    @PostMapping("/{accountNumber}/withdraw")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionResponseDto withdraw(
            @PathVariable String accountNumber,
            @Valid @RequestBody WithdrawalRequestDto request) {

        return transactionService.withdraw(accountNumber, request);
    }

    @PostMapping("/{sourceAccountNumber}/transfer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionResponseDto transfer(
            @PathVariable String sourceAccountNumber,
            @Valid @RequestBody TransferRequestDto request) {

        return transactionService.transfer(sourceAccountNumber, request);
    }

    @GetMapping("/{accountNumber}/transactions")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<TransactionResponseDto> getAccountTransactions(
            @PathVariable String accountNumber,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return transactionService.getAccountTransactions(
                accountNumber,
                pageable
        );
    }

    @GetMapping("/transactions/{reference}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionResponseDto getTransaction(
            @PathVariable String reference) {

        return transactionService.getTransactionByReference(reference);
    }
}
