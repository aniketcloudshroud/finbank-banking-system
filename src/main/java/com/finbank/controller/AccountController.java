package com.finbank.controller;

import com.finbank.dto.AccountResponseDto;
import com.finbank.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(
            AccountService accountService
    ) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public AccountResponseDto getAccount(
            @PathVariable String accountNumber
    ) {

        return accountService
                .getAccountByAccountNumber(
                        accountNumber
                );
    }

    /*
     * Customer's own accounts.
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<AccountResponseDto> getMyAccounts(
            Pageable pageable
    ) {

        return accountService
                .getCurrentCustomerAccounts(
                        pageable
                );
    }

    /*
     * Admin endpoint.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountResponseDto> getAllAccounts(
            Pageable pageable
    ) {

        return accountService
                .getAllAccounts(pageable);
    }
}