package com.habds.lcl.examples.simple;

import com.habds.lcl.core.data.EntityManagerRepository;
import com.habds.lcl.core.data.PagingAndSorting;
import com.habds.lcl.core.data.filter.Filter;
import com.habds.lcl.core.data.filter.impl.Equals;
import com.habds.lcl.core.data.filter.impl.In;
import com.habds.lcl.core.data.filter.impl.Like;
import com.habds.lcl.core.data.filter.impl.Range;
import com.habds.lcl.examples.config.AppConfig;
import com.habds.lcl.examples.dto.*;
import com.habds.lcl.examples.persistence.bo.*;
import com.habds.lcl.examples.persistence.dao.AccountDao;
import com.habds.lcl.examples.persistence.dao.ClientDao;
import com.habds.lcl.spring.JpaDao;
import com.habds.lcl.spring.SpringProcessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

import static com.habds.lcl.examples.persistence.bo.Gender.F;
import static com.habds.lcl.examples.persistence.bo.Gender.M;
import static org.junit.Assert.*;

/**
 * Test sorting&amp;filtering using H2 database
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 12/14/2015 9:00 PM
 */
@Transactional
@SuppressWarnings("SpringJavaAutowiringInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AppConfig.class)
public class SpringH2Test {

    public static final String CLIENT_EMAIL = "some@email.net";
    public static final Date BIRTHDAY = new GregorianCalendar(1992, Calendar.AUGUST, 2).getTime();

    @Autowired
    private SpringProcessor processor;
    @Autowired
    private ClientDao clientDao;
    @Autowired
    private AccountDao accountDao;

    @Autowired
    private EntityManagerRepository repo;

    @Before
    public void init() {
        Client lead = clientDao.save(new Client(new LoginData("lead@lead.com", "anotherpass"),
            new PersonalData("John", "Doe", new Date(), M), null));

        String email = CLIENT_EMAIL;

        Account checking = new Account("UAH", AccountType.CHECKING,
            new AccountState(BigDecimal.valueOf(1_000), false), new Date());
        Account savings = new Account("USD", AccountType.SAVINGS,
            new AccountState(BigDecimal.valueOf(1_000_000), true), new Date());

        Client client = new Client(new LoginData(email, "pass"),
            new PersonalData("Yurii", "Smyrnov", BIRTHDAY, M), null);
        client.addAccount(checking).addAccount(savings);
        client.setSelectedAccount(checking);
        lead.addReferral(client);
        clientDao.save(client);

        clientDao.save(new Client(null, new PersonalData("Yurii", "", new Date(), M), null));
        clientDao.save(
            new Client(new LoginData("abc1", null), new PersonalData("Abc", "cba", new Date(), F), null));
        clientDao.save(
            new Client(new LoginData("abc2", null), new PersonalData("Abc", "cba", new Date(), F), null));
        clientDao.save(
            new Client(new LoginData("abc3", null), new PersonalData("Abc", "cba", new Date(), F), null));
        clientDao.save(new Client(null, new PersonalData("test", "test", new Date(), M), null));

        Client other = new Client(null, new PersonalData("test Abc", "cba", new Date(), F), null);
        Account otherChecking = new Account("UAH", AccountType.CHECKING,
            new AccountState(BigDecimal.valueOf(10_000), false), new Date());
        other.addAccount(otherChecking).setSelectedAccount(otherChecking);
        clientDao.save(other);
    }

    @Test
    public void testCorrectInit() {
        System.out.println("Checking if clients were correctly created...");

        assertEquals(8, clientDao.count());

        Client client = clientDao
            .findOne((root, query, cb) -> cb.equal(root.get("loginData").get("email"), CLIENT_EMAIL));
        ClientDto clientDto = processor.process(client, ClientDto.class);
        assertEquals(BIRTHDAY, clientDto.getBirthday());
        assertTrue(clientDto.isAdult());
        assertEquals("Yurii", clientDto.getName());
        assertEquals("John", clientDto.getLeadName());
        assertEquals(CLIENT_EMAIL, clientDto.getLogin());
        assertEquals(M, clientDto.getGender());
        assertEquals(null, clientDto.getManagerName());
        assertEquals(1_000, clientDto.getSelectedAccount().getAmount().intValueExact());
        assertEquals("UAH", clientDto.getSelectedAccount().getCurrency());
        assertEquals(AccountType.CHECKING.name(), clientDto.getSelectedAccount().getType());
        assertEquals(client.getUid(), clientDto.getUid());

        assertTrue(clientDto.getAccounts().stream().allMatch(a -> a.getClass() == AccountDto.class));

        List<AccountDto> accounts = new ArrayList<>(clientDto.getAccounts());
        assertEquals(clientDto.getSelectedAccount().getNumber(), accounts.get(0).getNumber());
        assertEquals(AccountType.SAVINGS.name(), accounts.get(1).getType());

        assertArrayEquals(new AccountType[]{AccountType.CHECKING, AccountType.SAVINGS},
            clientDto.getOwnedAccountTypes().toArray(new AccountType[clientDto.getOwnedAccountTypes().size()]));

        System.out.println("Init test OK");
    }

    @Test
    public void testDTOFilteringForSpringDao() {
        System.out.println("Testing DTO filtering via Spring Data JpaSpecExecutor");

        JpaDao<Client, ClientSpecificationDto> dao = processor.dao(ClientSpecificationDto.class);
        ClientSpecificationDto filter = new ClientSpecificationDto();
        assertEquals(8, dao.count(filter));

        filter.setEmail(CLIENT_EMAIL);
        Client client = dao.getOne(filter);
        assertEquals(CLIENT_EMAIL, client.getLoginData().getEmail());
        assertEquals(BIRTHDAY, client.getPersonalData().getBirthday());

        filter = new ClientSpecificationDto();
        filter.setName("Yurii");
        filter.setBirthdayFrom(BIRTHDAY);
        assertEquals(2, dao.count(filter));

        filter = new ClientSpecificationDto();
        filter.setName("Abc");
        filter.setBirthdayTo(new Date());
        assertEquals(4, dao.count(filter));

        filter = new ClientSpecificationDto();
        filter.setGender(F);
        assertEquals(4, dao.count(filter));

        JpaDao<Client, ClientExtendableSpecificationDto> dao2 = processor.dao(ClientExtendableSpecificationDto.class);

        ClientExtendableSpecificationDto filter2 = new ClientExtendableSpecificationDto();
        filter2.setNames(Arrays.asList("Yurii", "Abc"));
        assertEquals(5, dao2.count(filter2));

        filter2 = new ClientExtendableSpecificationDto();
        filter2.setAmount(new BigDecimal(1_000));
        assertEquals(1, dao2.count(filter2));

        filter2 = new ClientExtendableSpecificationDto();
        filter2.setNames(Arrays.asList("Yurii", "Abc"));
        filter2.setOnlyAdults(true);
        assertEquals(1, dao2.count(filter2));

        System.out.println("DTO filtering via Spring Data JpaSpecExecutor OK");
    }

    @Test
    public void testDTOFilteringForEMRepo() {
        System.out.println("Testing DTO filtering via JPA EntityManager");

        ClientSpecificationDto filter = new ClientSpecificationDto();
        assertEquals(8, repo.count(filter));

        filter.setEmail(CLIENT_EMAIL);
        Client client = repo.getOne(filter);
        assertEquals(CLIENT_EMAIL, client.getLoginData().getEmail());
        assertEquals(BIRTHDAY, client.getPersonalData().getBirthday());

        filter = new ClientSpecificationDto();
        filter.setName("Yurii");
        filter.setBirthdayFrom(BIRTHDAY);
        assertEquals(2, repo.count(filter));

        filter = new ClientSpecificationDto();
        filter.setName("Abc");
        filter.setBirthdayTo(new Date());
        assertEquals(4, repo.count(filter));

        filter = new ClientSpecificationDto();
        filter.setGender(F);
        assertEquals(4, repo.count(filter));

        ClientExtendableSpecificationDto filter2 = new ClientExtendableSpecificationDto();
        filter2.setNames(Arrays.asList("Yurii", "Abc"));
        assertEquals(5, repo.count(filter2));

        filter2 = new ClientExtendableSpecificationDto();
        filter2.setAmount(new BigDecimal(1_000));
        assertEquals(1, repo.count(filter2));

        filter2 = new ClientExtendableSpecificationDto();
        filter2.setNames(Arrays.asList("Yurii", "Abc"));
        filter2.setOnlyAdults(true);
        assertEquals(1, repo.count(filter2));

        System.out.println("DTO filtering via JPA EntityManager OK");
    }

    @Test
    public void testFilteringForSpringDao() {
        System.out.println("Testing filtering via Spring Data JpaSpecExecutor");

        JpaDao<Client, ClientDto> dao = processor.dao(ClientDto.class);
        assertEquals(8, dao.count(new HashMap<>()));

        Map<String, Filter> filters = new HashMap<>();
        filters.put("login", new Equals(CLIENT_EMAIL));
        ClientDto client = dao.getOne(filters);
        assertEquals(CLIENT_EMAIL, client.getLogin());
        assertEquals(BIRTHDAY, client.getBirthday());

        filters = new HashMap<>();
        filters.put("name", new Equals("Yurii"));
        filters.put("birthday", new Range<Date, Date>().from(BIRTHDAY));
        assertEquals(2, dao.count(filters));

        filters = new HashMap<>();
        filters.put("name", new Equals("Yurii"));
        filters.put("birthday", new Range<Date, Date>().fromExclusive(BIRTHDAY).toExclusive(new Date()));
        assertEquals(1, dao.count(filters));
        assertFalse(dao.findAll(filters).get(0).isAdult());

        filters = new HashMap<>();
        filters.put("name", new Like("Abc"));
        filters.put("birthday", new Range<Date, Date>().to(new Date()));
        assertEquals(4, dao.count(filters));

        filters = new HashMap<>();
        filters.put("name", new In("Yurii", "Abc"));
        assertEquals(5, dao.count(filters));

        filters = new HashMap<>();
        filters.put("selectedAccount.amount", new Equals(1_000));
        assertEquals(1, dao.count(filters));

        filters = new HashMap<>();
        filters.put("gender", new Equals("F"));
        assertEquals(4, dao.count(filters));

        filters = new HashMap<>();
        filters.put("gender", new Equals(null));
        assertEquals(0, dao.count(filters));

        System.out.println("Filtering via Spring Data JpaSpecExecutor OK");
    }

    @Test
    public void testFilteringForEMRepo() {
        System.out.println("Testing filtering via JPA EntityManager");

        assertEquals(8, repo.count(new HashMap<>(), ClientDto.class));

        Map<String, Filter> filters = new HashMap<>();
        filters.put("login", new Equals(CLIENT_EMAIL));
        ClientDto client = repo.getOne(filters, ClientDto.class);
        assertEquals(CLIENT_EMAIL, client.getLogin());
        assertEquals(BIRTHDAY, client.getBirthday());

        filters = new HashMap<>();
        filters.put("name", new Equals("Yurii"));
        filters.put("birthday", new Range<Date, Date>().from(BIRTHDAY));
        assertEquals(2, repo.count(filters, ClientDto.class));

        filters = new HashMap<>();
        filters.put("name", new Equals("Yurii"));
        filters.put("birthday", new Range<Date, Date>().fromExclusive(BIRTHDAY).toExclusive(new Date()));
        assertEquals(1, repo.count(filters, ClientDto.class));

        filters = new HashMap<>();
        filters.put("name", new Like("Abc"));
        filters.put("birthday", new Range<Date, Date>().to(new Date()));
        assertEquals(4, repo.count(filters, ClientDto.class));

        filters = new HashMap<>();
        filters.put("name", new In("Yurii", "Abc"));
        assertEquals(5, repo.count(filters, ClientDto.class));

        filters = new HashMap<>();
        filters.put("selectedAccount.amount", new Equals(1_000));
        assertEquals(1, repo.count(filters, ClientDto.class));

        filters = new HashMap<>();
        filters.put("gender", new Equals("F"));
        assertEquals(4, repo.count(filters, ClientDto.class));

        filters = new HashMap<>();
        filters.put("gender", new Equals(null));
        assertEquals(0, repo.count(filters, ClientDto.class));

        System.out.println("Filtering via JPA EntityManager OK");
    }

    @Test
    public void testFilteringAndSortingForSpringDao() {
        System.out.println("Testing filtering and sorting via Spring Data JpaSpecExecutor");

        JpaDao<Client, ClientDto> dao = processor.dao(ClientDto.class);

        Pageable pageable = new PageRequest(0, 5, new Sort(Sort.Direction.DESC, "name").and(new Sort("login")));
        Map<String, Filter> filters = new HashMap<>();
        filters.put("name", new Like("Yurii").negate());
        List<ClientDto> clients = dao.findAll(filters, pageable).getContent();

        assertEquals(5, clients.size());
        assertEquals(0, clients.stream().filter(c -> c.getName().equals("Yurii")).count());

        assertEquals("test Abc", clients.get(0).getName());
        assertEquals("test", clients.get(1).getName());
        assertEquals("John", clients.get(2).getName());
        assertEquals("abc1", clients.get(3).getLogin());
        assertEquals("abc2", clients.get(4).getLogin());

        Page<ClientDto> page = dao.findAll(new HashMap<>(), new PageRequest(0, 1, new Sort("uid")));
        assertEquals(1, page.getNumberOfElements());
        assertEquals("lead@lead.com", page.getContent().get(0).getLogin());

        clients = dao.findAll(new HashMap<>(), new Sort(Sort.Direction.DESC, "selectedAccount.amount"));
        assertEquals(8, clients.size());
        assertEquals("test Abc", clients.get(0).getName());
        assertEquals("Yurii", clients.get(1).getName());

        // FIXME: Sorting on field like @ManyToOne-@OneToOne-@ManyToOne-String doesn't work properly on Spring Data JPA,
        // FIXME: returns only records without nulls. Probably raise a bug in JIRA?

        System.out.println("Filtering and sorting via Spring Data JpaSpecExecutor OK");
    }

    @Test
    public void testFilteringAndSortingForEMRepo() {
        System.out.println("Testing filtering and sorting via JPA EntityManager");

        Map<String, Filter> filters = new HashMap<>();
        filters.put("name", new Like("Yurii").negate());
        List<ClientDto> clients = repo.getAll(
            filters,
            new PagingAndSorting().withPagination(0, 5).orderBy("name", false).orderBy("login"),
            ClientDto.class);
        assertEquals(5, clients.size());
        assertEquals(0, clients.stream().filter(c -> c.getName().equals("Yurii")).count());

        assertEquals("test Abc", clients.get(0).getName());
        assertEquals("test", clients.get(1).getName());
        assertEquals("John", clients.get(2).getName());
        assertEquals("abc1", clients.get(3).getLogin());
        assertEquals("abc2", clients.get(4).getLogin());

        clients = repo.getAll(new HashMap<>(), new PagingAndSorting().withPagination(0, 1).orderBy("uid"),
            ClientDto.class);
        assertEquals(1, clients.size());
        assertEquals("lead@lead.com", clients.get(0).getLogin());

        clients = repo.getAll(new HashMap<>(), new PagingAndSorting().orderBy("selectedAccount.amount", false),
            ClientDto.class);
        assertEquals(8, clients.size());
        assertEquals("test Abc", clients.get(0).getName());
        assertEquals("Yurii", clients.get(1).getName());

        System.out.println("Filtering and sorting via JPA EntityManager OK");
    }

    @Test
    public void testCreateAndUpdate() {
        System.out.println("Testing create&update via Spring Data JpaSpecExecutor");

        JpaDao<Client, NewClientDto> newClientDao = processor.dao(NewClientDto.class);
        JpaDao<Client, UpdateClientDto> updateClientDao = processor.dao(UpdateClientDto.class);

        Date birthday = new Date();
        NewClientDto newClient = new NewClientDto("test", "test name", birthday, M);
        Map<String, Object> predefined = new HashMap<>();
        predefined.put("loginData.password", "password");

        Client client = newClientDao.create(newClient, predefined);

        assertEquals(newClient.getName(), client.getPersonalData().getName());
        assertEquals(newClient.getLogin(), client.getLoginData().getEmail());
        assertEquals(newClient.getGender(), client.getPersonalData().getGender());
        assertEquals(newClient.getBirthday(), client.getPersonalData().getBirthday());
        assertNull(client.getSelectedAccount());

        Account account = new Account("EUR", AccountType.CHECKING,
            new AccountState(BigDecimal.ZERO), new Date(666));
        account = accountDao.save(account);

        UpdateClientDto updateRequest = new UpdateClientDto("other name", null, F, account.getNumber());
        client = updateClientDao.update(client, updateRequest);

        assertEquals(updateRequest.getName(), client.getPersonalData().getName());
        assertEquals(updateRequest.getGender(), client.getPersonalData().getGender());
        assertNull(client.getPersonalData().getBirthday());
        assertEquals(updateRequest.getSelectedAccount(), client.getSelectedAccount().getNumber());
        assertEquals(account.getCreationDate(), client.getSelectedAccount().getCreationDate());

        System.out.println("Create&update via Spring Data JpaSpecExecutor OK");
    }
}
