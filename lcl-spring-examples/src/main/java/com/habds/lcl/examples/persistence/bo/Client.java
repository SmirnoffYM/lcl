package com.habds.lcl.examples.persistence.bo;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Client extends User {

    @Embedded
    private PersonalData personalData;

    @OneToMany(mappedBy = "client", cascade = CascadeType.PERSIST)
    private List<Account> accounts = new ArrayList<>();
    @OneToOne
    private Account selectedAccount;
    @OneToMany(mappedBy = "lead")
    private List<Client> referrals;
    @ManyToOne
    private Client lead;
    @ManyToOne
    private Manager manager;

    private Client() {
    }

    public Client(LoginData loginData, PersonalData personalData, Manager manager) {
        this.loginData = loginData;
        this.personalData = personalData;
        this.manager = manager;
    }

    public PersonalData getPersonalData() {
        return personalData;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Client addAccount(Account account) {
        accounts.add(account);
        account.setClient(this);
        return this;
    }

    public Client addReferral(Client referral) {
        referral.lead = this;
        if (referrals == null) {
            referrals = new ArrayList<>();
        }
        referrals.add(referral);
        return this;
    }

    public List<Client> getReferrals() {
        return referrals;
    }

    public Client getLead() {
        return lead;
    }

    public Manager getManager() {
        return manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Account getSelectedAccount() {
        return selectedAccount;
    }

    public void setSelectedAccount(Account selectedAccount) {
        this.selectedAccount = selectedAccount;
        selectedAccount.setClient(this);
        if (!accounts.contains(selectedAccount)) {
            accounts.add(selectedAccount);
        }
    }
}
