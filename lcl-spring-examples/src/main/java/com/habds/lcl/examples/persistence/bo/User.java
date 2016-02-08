package com.habds.lcl.examples.persistence.bo;

import javax.persistence.*;

@MappedSuperclass
public abstract class User {

    @Id
    @GeneratedValue
    protected long uid;
    @Embedded
    protected LoginData loginData;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public LoginData getLoginData() {
        return loginData;
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
    }
}
