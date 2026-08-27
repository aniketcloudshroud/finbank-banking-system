package com.finbank.dto;

import jakarta.validation.constraints.NotBlank;

public class KycRequestDto {

    @NotBlank
    private String documentType;

    @NotBlank
    private String documentNumber;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
}