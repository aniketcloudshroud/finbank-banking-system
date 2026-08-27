package com.finbank.controller;

import com.finbank.dto.*;
import com.finbank.service.*;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{accountNumber}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public AccountResponseDto getAccount(
            @PathVariable String accountNumber) {

        return accountService.getAccountByAccountNumber(accountNumber);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountResponseDto> getAllAccounts(Pageable pageable) {

        return accountService.getAllAccounts(pageable);
    }

}