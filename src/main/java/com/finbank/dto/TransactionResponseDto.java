package com.finbank.dto;

import com.finbank.entity.*;
import java.math.*;
import java.time.*;

public class TransactionResponseDto {

    private String reference;
    private TransactionType type;
    private BigDecimal amount;
    private TransactionStatus status;
    private String description;
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TransactionResponseDto(Transaction transaction) {
        this.reference = transaction.getReference();
        this.type = transaction.getType();
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus();
        this.description = transaction.getDescription();

        if (transaction.getSourceAccount() != null) {
            this.sourceAccountNumber =
                    transaction.getSourceAccount().getAccountNumber();
        }

        if (transaction.getDestinationAccount() != null) {
            this.destinationAccountNumber =
                    transaction.getDestinationAccount().getAccountNumber();
        }

        this.createdAt = transaction.getCreatedAt();
        this.updatedAt = transaction.getUpdatedAt();
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public void setSourceAccountNumber(String sourceAccountNumber) {
        this.sourceAccountNumber = sourceAccountNumber;
    }

    public String getDestinationAccountNumber() {
        return destinationAccountNumber;
    }

    public void setDestinationAccountNumber(String destinationAccountNumber) {
        this.destinationAccountNumber = destinationAccountNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}