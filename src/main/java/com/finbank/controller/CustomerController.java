package com.finbank.controller;

import com.finbank.dto.AccountRequestDto;
import com.finbank.dto.AccountResponseDto;
import com.finbank.dto.CustomerRequestDto;
import com.finbank.dto.CustomerResponseDto;
import com.finbank.dto.CustomerUpdateRequestDto;
import com.finbank.dto.KycRejectionRequestDto;
import com.finbank.dto.KycRequestDto;
import com.finbank.dto.KycResponseDto;
import com.finbank.service.AccountService;
import com.finbank.service.CustomerService;
import com.finbank.service.KycService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final AccountService accountService;

    /*
     * Kept because KYC already exists in your project.
     * We are not expanding KYC in Phase 1.
     */
    private final KycService kycService;

    public CustomerController(
            CustomerService customerService,
            AccountService accountService,
            KycService kycService
    ) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.kycService = kycService;
    }

    @PostMapping
    public CustomerResponseDto createCustomer(
            @Valid @RequestBody CustomerRequestDto request
    ) {

        return customerService.createCustomer(request);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public CustomerResponseDto getCurrentCustomer() {

        return customerService.getCurrentCustomer();
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@customerService.isCurrentCustomer(#id)"
    )
    public CustomerResponseDto getCustomer(
            @PathVariable Long id
    ) {

        return customerService.getCustomerById(id);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CustomerResponseDto> getAllCustomers(
            Pageable pageable
    ) {

        return customerService.getAllCustomers(
                pageable
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize(
            "hasRole('ADMIN') or " +
                    "@customerService.isCurrentCustomer(#id)"
    )
    public CustomerResponseDto updateCustomer(
            @PathVariable Long id,
            @Valid
            @RequestBody CustomerUpdateRequestDto request
    ) {

        return customerService.updateCustomer(
                id,
                request
        );
    }

    @PostMapping("/{customerId}/accounts")
    @PreAuthorize(
            "hasRole('CUSTOMER') and " +
                    "@customerService.isCurrentCustomer(#customerId)"
    )
    public AccountResponseDto createAccount(
            @PathVariable Long customerId,
            @Valid @RequestBody AccountRequestDto request
    ) {

        return accountService.createAccount(
                customerId,
                request
        );
    }

    @GetMapping("/{customerId}/accounts")
    @PreAuthorize(
            "hasRole('CUSTOMER') and " +
                    "@customerService.isCurrentCustomer(#customerId)"
    )
    public Page<AccountResponseDto> getCustomerAccounts(
            @PathVariable Long customerId,
            Pageable pageable
    ) {

        return accountService.getCurrentCustomerAccounts(
                pageable
        );
    }

    /*
     * Existing KYC functionality.
     * Intentionally left untouched for now.
     */

    @PostMapping("/{customerId}/kyc")
    @PreAuthorize(
            "hasRole('CUSTOMER') and " +
                    "@customerService.isCurrentCustomer(#customerId)"
    )
    public KycResponseDto submitKyc(
            @PathVariable Long customerId,
            @Valid @RequestBody KycRequestDto request
    ) {

        return kycService.submitKyc(
                customerId,
                request
        );
    }

    @GetMapping("/{customerId}/kyc")
    @PreAuthorize(
            "hasRole('CUSTOMER') and " +
                    "@customerService.isCurrentCustomer(#customerId)"
    )
    public KycResponseDto getKyc(
            @PathVariable Long customerId
    ) {

        return kycService.getKyc(customerId);
    }
}