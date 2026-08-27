package com.finbank.dto;

import com.finbank.entity.Customer;
import com.finbank.entity.KycStatus;

import java.time.LocalDateTime;

public class KycResponseDto {

    private Long customerId;
    private KycStatus status;
    private String documentType;
    private String documentNumber;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String rejectionReason;

    public KycResponseDto(Customer customer) {
        this.customerId = customer.getId();
        this.status = customer.getKycStatus();
        this.documentType = customer.getKycDocumentType();
        this.documentNumber = customer.getKycDocumentNumber();
        this.submittedAt = customer.getKycSubmittedAt();
        this.reviewedAt = customer.getKycReviewedAt();
        this.rejectionReason = customer.getKycRejectionReason();
    }

    public Long getCustomerId() {
        return customerId;
    }

    public KycStatus getStatus() {
        return status;
    }

    public String getDocumentType() {
        return documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}