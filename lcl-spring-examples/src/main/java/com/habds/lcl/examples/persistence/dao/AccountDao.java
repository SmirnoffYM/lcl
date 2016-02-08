package com.habds.lcl.examples.persistence.dao;

import com.habds.lcl.examples.persistence.bo.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountDao extends JpaRepository<Account, String>, JpaSpecificationExecutor<Account> {
}
