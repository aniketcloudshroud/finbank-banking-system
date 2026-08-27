package com.finbank.dto;


import com.finbank.entity.*;
import jakarta.validation.constraints.*;

public class AccountRequestDto {

    @NotNull
    private AccountType accountType;

    @NotNull
    private Currency currency;

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}


