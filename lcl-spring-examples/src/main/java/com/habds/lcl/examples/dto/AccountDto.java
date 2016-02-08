package com.habds.lcl.examples.dto;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.annotation.Link;
import com.habds.lcl.examples.persistence.bo.Account;

import java.math.BigDecimal;

@ClassLink(Account.class)
public class AccountDto {

    private String number;
    @Link("state.amount")
    private BigDecimal amount;
    @Link("currencyCode")
    private String currency;
    private String type;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
