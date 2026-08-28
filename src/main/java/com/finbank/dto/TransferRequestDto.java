package com.finbank.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TransferRequestDto {

    @NotBlank(message = "Destination account number is required")
    private String destinationAccountNumber;

    @DecimalMin(
            value = "0.01",
            message = "Amount must be greater than zero"
    )
    @Digits(
            integer = 17,
            fraction = 2,
            message = "Amount can have at most 2 decimal places"
    )
    private BigDecimal amount;

    @Size(
            max = 255,
            message = "Description cannot exceed 255 characters"
    )
    private String description;

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(
            String destinationAccountNumber
    ) {
        this.destinationAccountNumber =
                destinationAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}