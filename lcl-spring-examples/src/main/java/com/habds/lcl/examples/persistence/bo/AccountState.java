package com.habds.lcl.examples.persistence.bo;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
public class AccountState {

    @Id
    @GeneratedValue
    private long id;

    private BigDecimal amount;
    private boolean locked;

    public AccountState() {
    }

    public AccountState(BigDecimal amount) {
        this(amount, false);
    }

    public AccountState(BigDecimal amount, boolean locked) {
        this.amount = amount;
        this.locked = locked;
    }

    public long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public boolean isLocked() {
        return locked;
    }
}
