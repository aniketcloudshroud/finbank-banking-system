package com.finbank.controller;

import com.finbank.dto.*;
import com.finbank.entity.*;
import com.finbank.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@EnableMethodSecurity
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final KycService kycService;

    public CustomerController(CustomerService customerService,
                              AccountService accountService,
                              KycService kycService) {

        this.customerService = customerService;
        this.accountService = accountService;
        this.kycService = kycService;
    }


    @PostMapping
    public CustomerResponseDto createCustomer(
            @Valid @RequestBody CustomerRequestDto request) {

        return customerService.createCustomer(request);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @customerService.isCurrentCustomer(#id)")
    public CustomerResponseDto getCustomer(
            @PathVariable Long id) {

        return customerService.getCustomerById(id);
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CustomerResponseDto> getAllCustomers(
            Pageable pageable) {

        return customerService.getAllCustomers(pageable);
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @customerService.isCurrentCustomer(#id)")
    public CustomerResponseDto updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerUpdateRequestDto requestDto) {

        return customerService.updateCustomer(id, requestDto);
    }


    @PostMapping("/{customerId}/accounts")
    @PreAuthorize("hasRole('ADMIN') or @customerService.isCurrentCustomer(#customerId)")
    public AccountResponseDto createAccount(
            @PathVariable Long customerId,
            @Valid @RequestBody AccountRequestDto requestDto) {

        return accountService.createAccount(customerId, requestDto);
    }


    @PatchMapping("/{customerId}/kyc")
    @PreAuthorize("hasRole('ADMIN')")
    public CustomerResponseDto updateKycStatus(
            @PathVariable Long customerId,
            @RequestParam KycStatus status) {

        return customerService.updateKycStatus(
                customerId,
                status
        );
    }

    @PostMapping("/{customerId}/kyc")
    @PreAuthorize("hasRole('CUSTOMER')")
    public KycResponseDto submitKyc(
            @PathVariable Long customerId,
            @Valid @RequestBody KycRequestDto request
    ) {
        return kycService.submitKyc(customerId, request);
    }

    @GetMapping("/{customerId}/kyc")
    @PreAuthorize("hasRole('CUSTOMER')")
    public KycResponseDto getKyc(
            @PathVariable Long customerId
    ) {
        return kycService.getKyc(customerId);
    }
}