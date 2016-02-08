package com.habds.lcl.examples.persistence.dao;

import com.habds.lcl.examples.persistence.bo.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientDao extends JpaRepository<Client, Long>, JpaSpecificationExecutor<Client> {
}
