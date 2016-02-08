package com.habds.lcl.examples.persistence.bo;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Manager extends User {

    @Embedded
    private PersonalData personalData;
    @OneToMany(mappedBy = "manager")
    private List<Client> managed;

    protected Manager() {

    }

    public List<Client> getManaged() {
        return managed;
    }

    public void setManaged(List<Client> managed) {
        this.managed = managed;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    public void setPersonalData(PersonalData personalData) {
        this.personalData = personalData;
    }
}
