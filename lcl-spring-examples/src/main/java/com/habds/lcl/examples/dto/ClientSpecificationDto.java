package com.habds.lcl.examples.dto;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.annotation.Link;
import com.habds.lcl.core.data.filter.*;
import com.habds.lcl.examples.persistence.bo.AccountType;
import com.habds.lcl.examples.persistence.bo.Client;
import com.habds.lcl.examples.persistence.bo.Gender;

import java.util.Date;
import java.util.List;

@ClassLink(Client.class)
public class ClientSpecificationDto {

    @Link("loginData.email")
    private String email;

    @Like
    @Link("personalData.name")
    private String name;

    @From
    @Link("personalData.birthday")
    private Date birthdayFrom;

    @To
    @Link("personalData.birthday")
    private Date birthdayTo;

    @Link("personalData.gender")
    private Gender gender;

    @In
    @Link("accounts.type")
    private List<AccountType> ownedAccountTypes;

    @IsNull
    @Link("selectedAccount")
    private Boolean selectedAccountAbsent;

    private AccountDto selectedAccount;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthdayFrom() {
        return birthdayFrom;
    }

    public void setBirthdayFrom(Date birthdayFrom) {
        this.birthdayFrom = birthdayFrom;
    }

    public Date getBirthdayTo() {
        return birthdayTo;
    }

    public void setBirthdayTo(Date birthdayTo) {
        this.birthdayTo = birthdayTo;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<AccountType> getOwnedAccountTypes() {
        return ownedAccountTypes;
    }

    public void setOwnedAccountTypes(List<AccountType> ownedAccountTypes) {
        this.ownedAccountTypes = ownedAccountTypes;
    }

    public Boolean getSelectedAccountAbsent() {
        return selectedAccountAbsent;
    }

    public void setSelectedAccountAbsent(Boolean selectedAccountAbsent) {
        this.selectedAccountAbsent = selectedAccountAbsent;
    }

    public AccountDto getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(AccountDto selectedAccount) {
        this.selectedAccount = selectedAccount;
    }
}
