package com.habds.lcl.examples.mvc;

import com.habds.lcl.core.data.filter.Filter;
import com.habds.lcl.examples.dto.ClientDto;
import com.habds.lcl.examples.dto.ClientSpecificationDto;
import com.habds.lcl.examples.dto.NewClientDto;
import com.habds.lcl.examples.dto.UpdateClientDto;
import com.habds.lcl.examples.persistence.bo.Account;
import com.habds.lcl.examples.persistence.bo.AccountState;
import com.habds.lcl.examples.persistence.bo.AccountType;
import com.habds.lcl.examples.persistence.bo.Client;
import com.habds.lcl.examples.persistence.dao.AccountDao;
import com.habds.lcl.spring.JpaDao;
import com.habds.lcl.spring.SpringProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Demo controller managing Clients
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/31/16 12:26 AM
 */
@RestController
@RequestMapping("/clients/")
public class ClientController {

    @Autowired
    private SpringProcessor processor;
    @Autowired
    private AccountDao accountDao;

    @RequestMapping(value = "v1/", method = RequestMethod.POST)
    private Page<ClientDto> read(@RequestBody Map<String, Filter> filters, Pageable pageable) {
        return processor.dao(ClientDto.class).findAll(filters, pageable);
    }

    @RequestMapping(value = "v2/", method = RequestMethod.POST)
    private Page<ClientDto> read(@RequestBody ClientSpecificationDto filter, Pageable pageable) {
        return processor.dao(ClientSpecificationDto.class).findAll(filter, pageable, ClientDto.class);
    }

    @RequestMapping(method = RequestMethod.PUT)
    private ClientDto create(@RequestBody NewClientDto dto) {
        JpaDao<Client, NewClientDto> dao = processor.dao(NewClientDto.class);

        Map<String, Object> predefined = new HashMap<>();
        predefined.put("loginData.password", "password");

        Client client = dao.createAndSave(dto, predefined);

        client.setSelectedAccount(
            accountDao.save(new Account("USD", AccountType.CHECKING, new AccountState(BigDecimal.ZERO), new Date())));

        return processor.process(dao.repo().save(client), ClientDto.class);
    }

    @RequestMapping(value = "{client}/", method = RequestMethod.POST)
    private ClientDto update(@PathVariable Client client, @RequestBody UpdateClientDto dto) {
        JpaDao<Client, UpdateClientDto> dao = processor.dao(UpdateClientDto.class);

        return processor.process(dao.updateAndSave(client, dto), ClientDto.class);
    }
}
