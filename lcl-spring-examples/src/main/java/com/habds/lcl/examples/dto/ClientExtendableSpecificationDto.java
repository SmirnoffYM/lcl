package com.habds.lcl.examples.dto;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.annotation.Ignored;
import com.habds.lcl.core.annotation.Link;
import com.habds.lcl.core.data.Specs;
import com.habds.lcl.core.data.filter.In;
import com.habds.lcl.examples.persistence.bo.Client;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@ClassLink(Client.class)
public class ClientExtendableSpecificationDto implements Specs<Client> {

    @In
    @Link("personalData.name")
    private List<String> names;

    @Link("selectedAccount.state.amount")
    private BigDecimal amount;

    @Ignored
    private Boolean onlyAdults;

    @Override
    public Predicate buildPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        if (onlyAdults != null) {
            return cb.lessThanOrEqualTo(root.get("personalData").get("birthday"),
                Date.from(ZonedDateTime.now().minusYears(18).toInstant()));
        }
        return cb.and();
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getOnlyAdults() {
        return onlyAdults;
    }

    public void setOnlyAdults(Boolean onlyAdults) {
        this.onlyAdults = onlyAdults;
    }
}
