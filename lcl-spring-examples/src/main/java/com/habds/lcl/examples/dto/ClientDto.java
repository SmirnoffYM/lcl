package com.habds.lcl.examples.dto;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.annotation.Contains;
import com.habds.lcl.core.annotation.Link;
import com.habds.lcl.examples.persistence.bo.Client;
import com.habds.lcl.examples.persistence.bo.Gender;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

@ClassLink(Client.class)
public class ClientDto {

    private long uid;
    @Link("loginData.email")
    private String login;
    @Link("personalData.name")
    private String name;
    @Link("personalData.birthday")
    private Date birthday;

    @Link("personalData.gender")
    private Gender gender;

    private AccountDto selectedAccount;
    @Contains(AccountDto.class)
    private Set<AccountDto> accounts = new LinkedHashSet<>();

    @Link("lead.personalData.name")
    private String leadName;
    @Link("manager.personalData.name")
    private String managerName;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Set<AccountDto> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<AccountDto> accounts) {
        this.accounts = accounts;
    }

    public String getLeadName() {
        return leadName;
    }

    public void setLeadName(String leadName) {
        this.leadName = leadName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public AccountDto getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(AccountDto selectedAccount) {
        this.selectedAccount = selectedAccount;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
