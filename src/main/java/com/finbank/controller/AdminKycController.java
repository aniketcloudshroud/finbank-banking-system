package com.finbank.controller;

import com.finbank.dto.*;
import com.finbank.service.KycService;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/kyc")
@PreAuthorize("hasRole('ADMIN')")
public class AdminKycController {

    private final KycService kycService;

    public AdminKycController(KycService kycService) {
        this.kycService = kycService;
    }

    @GetMapping
    public Page<KycResponseDto> getPendingKyc(
            Pageable pageable
    ) {
        return kycService.getPendingKyc(pageable);
    }


    @PutMapping("/{customerId}/approve")
    public KycResponseDto approveKyc(
            @PathVariable Long customerId
    ) {
        return kycService.approveKyc(customerId);
    }


    @PutMapping("/{customerId}/reject")
    public KycResponseDto rejectKyc(
            @PathVariable Long customerId,
            @Valid @RequestBody KycRejectionRequestDto request
    ) {
        return kycService.rejectKyc(
                customerId,
                request.getReason()
        );
    }
}