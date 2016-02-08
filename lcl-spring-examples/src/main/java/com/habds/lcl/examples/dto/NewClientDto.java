package com.habds.lcl.examples.dto;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.annotation.Link;
import com.habds.lcl.examples.persistence.bo.Client;
import com.habds.lcl.examples.persistence.bo.Gender;

import java.util.Date;

@ClassLink(Client.class)
public class NewClientDto {

    @Link("loginData.email")
    private String login;
    @Link("personalData.name")
    private String name;
    @Link("personalData.birthday")
    private Date birthday;

    @Link("personalData.gender")
    private Gender gender;

    public NewClientDto() {
    }

    public NewClientDto(String login, String name, Date birthday, Gender gender) {
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.gender = gender;
    }

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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
