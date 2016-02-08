package com.habds.lcl.examples.persistence.bo;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
public class Account {

    @Id
    private String number = UUID.randomUUID().toString();
    private String currencyCode;
    private Date creationDate = new Date();
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @OneToOne(cascade = CascadeType.ALL)
    private AccountState state;
    @ManyToOne
    private Client client;

    protected Account() {
    }

    public Account(String currencyCode, AccountType type, AccountState state, Date creationDate) {
        this.currencyCode = currencyCode;
        this.type = type;
        this.state = state;
        this.creationDate = creationDate;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public AccountState getState() {
        return state;
    }

    public void setState(AccountState state) {
        this.state = state;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return number != null ? number.equals(account.number) : account.number == null;

    }

    @Override
    public int hashCode() {
        return number != null ? number.hashCode() : 0;
    }
}
