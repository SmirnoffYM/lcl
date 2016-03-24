package com.habds.lcl.examples.dto;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.annotation.Link;
import com.habds.lcl.examples.persistence.bo.Client;

import java.util.Date;

@ClassLink(Client.class)
public class UpdateClientDto {

    @Link("personalData.name")
    private String name;
    @Link("personalData.birthday")
    private Date birthday;
    @Link("personalData.gender")
    private String gender;

    @Link("selectedAccount.number")
    private String selectedAccount;

    public UpdateClientDto() {
    }

    public UpdateClientDto(String name, Date birthday, String gender, String selectedAccount) {
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
        this.selectedAccount = selectedAccount;
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(String selectedAccount) {
        this.selectedAccount = selectedAccount;
    }
}
