package com.habds.lcl.examples.persistence.bo;

import javax.persistence.*;
import java.util.Date;

@Embeddable
public class PersonalData {

    private String name;
    private String surname;
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthday;
    private Gender gender;

    private PersonalData() {
    }

    public PersonalData(String name, String surname, Date birthday, Gender gender) {
        this.name = name;
        this.surname = surname;
        this.birthday = birthday;
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Gender getGender() {
        return gender;
    }
}
