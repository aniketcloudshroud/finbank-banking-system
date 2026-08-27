package com.finbank.dto;

import jakarta.validation.constraints.NotBlank;

public class KycRejectionRequestDto {

    @NotBlank
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}