# lcl [![Build Status](https://travis-ci.org/SmirnoffYM/lcl.svg?branch=master)](https://travis-ci.org/SmirnoffYM/lcl)

LCL (stands for *Lightweight Class Linking*) is a small library that aims to simplify data interchange between 
Domain Objects (Business Objects, Entities) and Data Transfer Objects (DTOs, Command objects) in 
Java enterprise projects.

It's very common that you have ``User`` class with lots of relations: embedded personal data, references to
log entities (changes of that data), owned banking accounts, other users etc. So when sending all that data to frontend
(especially to public pages) you need to convert each ``User`` into ``UserDto`` object 
containing only fields that would be displayed on UI. Usually you just write up to 20 lines of something like
``dto.setName(user.getPersonalData().getName());`` to fill your DTO with data.

Also typically you need to provide possibility to filter, sort and paginate these DTOs. 
So to solve this you write your own JPA Specification (using Spring Data JPA) or 
even worse - custom Criteria Queries. All of this generate tons of boilerplate code when one ``Users`` page becomes 
10 pages with complex entities holding many fields and relations.

Let's see an example of such Domain Objects:

[Client](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/persistence/bo/Client.java):

```java

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
        
        ...
    }
```

[User](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/persistence/bo/User.java):

```java

    @MappedSuperclass
    public abstract class User {
    
        @Id
        @GeneratedValue
        protected long uid;
        @Embedded
        protected LoginData loginData;
        
        ...
    }
```

[Account](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/persistence/bo/Account.java):

```java

    @Entity
    public class Account {
    
        @Id
        private String number = UUID.randomUUID().toString();
        private String currencyCode;
        private Date creationDate = new Date();
        @Enumerated(EnumType.STRING)
        private AccountType type;
    
        @OneToOne(cascade = CascadeType.PERSIST)
        private AccountState state;
        @ManyToOne
        private Client client;
            
        ...
    }
```

So you want send list of ``ClientDto``s to your frontend (for example rich AngularJS application) with possibility to
filter, sort and paginate them by any of their properties using ``ClientSpecificationDto`` filter object. 

```java

    public class ClientDto {
    
        private long uid;
        private String login;
        private String name;
        private Date birthday;
        
        private boolean adult;
    
        private Gender gender;
    
        private AccountDto selectedAccount;
        private Set<AccountDto> accounts;
        
        private List<AccountType> ownedAccountTypes;
    
        private String leadName;
        private String managerName;

        ...
    }
```

```java

    public class ClientSpecificationDto implements Specification<Client> {
    
        private String email;
        private String name;
        private Date birthdayFrom;
        private Date birthdayTo;
        private Gender gender;
        private List<AccountType> ownedAccountTypes;
        private Boolean selectedAccountAbsent;

        // Your toPredicate() implementation here
        ...
    }
```

When using Spring you probably write your own ``toPredicate()`` implementation for each filter class like 
``ClientSpecificationDto``, 
set up ``Sort`` object, then call ``clientDAO.findAll(specification, pageable)`` and 
manually map the resulting Page<Client> into Page<ClientDto> 
setting up all 11 properties like this: ``dto.setLogin(client.getLoginData().getEmail())``. 
Too complex as for me - LCL allows you to throw out most of the boilerplate.

# Configuration
## JSF and EJB 3

First of all, add ``lcl-core`` into your project. Here's Maven dependency:
                                                                        
```xml

    <dependency>
        <groupId>com.habds</groupId>
        <artifactId>lcl-core</artifactId>
        <version>1.0.1.RELEASE</version>
    </dependency>
```

Declare ``DtoProcessor`` singleton bean and register all your DTOs in its ``@PostConstruct`` method.

```java

    @Singleton
    public class DtoProcessor extends SimpleProcessor {
    
        @PersistenceContext
        private EntityManager em;
    
        @PostConstruct
        public void init() {
            linkProcessor = new JpaLinkProcessor(em);
            
            // Register all your DTOs here
            // Better do it using Reflections library than manually
            add(ClientDto.class, NewClientDto.class, UpdateClientDto.class, AccountDto.class, 
                ClientSpecificationDto.class, ClientExtendableSpecificationDto)
                .configure();
        }
    }
```

Now, declare ``DtoRepo`` singleton bean, you will use it to manipulate your DTOs:

```java

    @Singleton
    public class DtoRepo extends EntityManagerRepository {
    
        @EJB
        public void setProcessor(DtoProcessor processor) {
            super.setProcessor(processor);
        }
    
        @PersistenceContext
        @Override
        public void setEm(EntityManager em) {
            super.setEm(em);
        }
    }
```

That's all, configuration is completed.

## Spring MVC

For Spring MVC ``lcl-core`` won't be enough, you will need to add ``lcl-spring`` into your project:

```xml

    <dependency>
        <groupId>com.habds</groupId>
        <artifactId>lcl-spring</artifactId>
        <version>1.0.1.RELEASE</version>
    </dependency>
```

Next, set up Jackson to allow deserialization of incoming filters and register ``SpringProcessor`` bean to deal 
with transforming, filtering and sorting:

```java

    @EnableWebMvc
    @SpringBootApplication
    public class App extends WebMvcConfigurerAdapter {
    
        @Autowired
        private Jackson2ObjectMapperBuilder jacksonBuilder;
    
        @Bean
        public SpringProcessor processor() {
            return (SpringProcessor) new SpringProcessor().add("com.habds.lcl.examples").configure();
        }    
    
        @Bean
        public Jackson2ObjectMapperBuilder jacksonBuilder(FilterDeserializer filterDeserializer) {
            Jackson2ObjectMapperBuilder b = new Jackson2ObjectMapperBuilder();
            b.deserializerByType(Filter.class, filterDeserializer);
            return b;
        }
    
        @Bean
        public FilterDeserializer filterDeserializer() {
            return new FilterDeserializer();
        }
    
        @Override
        public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
            converters.add(new MappingJackson2HttpMessageConverter(jacksonBuilder.build()));
        }
    }
    
```

That's all, configuration is completed.

# Filtering and sorting usage

Equip your ``ClientDto`` (class containing the data that will be send to frontend) 
with ``@ClassLink`` and ``@Link`` annotations.

[ClientDto](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/dto/ClientDto.java):

```java

    @ClassLink(Client.class)
    public class ClientDto {
    
        private long uid;
        @Link("loginData.email")
        private String login;
        @Link("personalData.name")
        private String name;
        @Link("personalData.birthday")
        private Date birthday;
        
        @Link("personalData.already18")
        private boolean adult;
    
        @Link("personalData.gender")
        private Gender gender;
    
        private AccountDto selectedAccount;
        @Contains(AccountDto.class)
        private Set<AccountDto> accounts = new LinkedHashSet<>();
        
        @Link("accounts.type")
        private List<AccountType> ownedAccountTypes;
    
        @Link("lead.personalData.name")
        private String leadName;
        @Link("manager.personalData.name")
        private String managerName;

        ...
    }
```

Here ``@Link("loginData.email") private String login;`` means that DTO's ``login`` property value will be taken from 
``email`` field of ``LoginData`` embeddable object belonging to ``User`` (superclass of ``Client``). 
If ``@Link`` annotation is absent or present but has no value provided, then it will mean that Entity's property 
has the same name as DTO's one. For example: ``uid`` property value in ``ClientDto`` class will be taken from
``uid`` property of ``User`` (superclass of ``Client``). To prevent LCL from managing some property, simply annotate it
with ``@Ignored``. 
You are free to map any relation property into another DTO - ``private AccountDto selectedAccount;``. But if your
relation is of collection type, you will need to explicitly tell LCL that DTO type using ``@Contains`` annotation - 
see ``accounts`` field.
Last, but not the least, you can specify XXX property in the ``@Link`` annotation when Entity has no XXX field at all, 
but has getXXX() method - look at ``adult`` field. 

Now complete your ``ClientSpecificationDto`` (class containing the filtering parameters received from frontend).

[ClientSpecificationDto](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/dto/ClientSpecificationDto.java):

```java

    @ClassLink(Client.class)
    public class ClientSpecificationDto {
    
        @Link("loginData.email")
        private String email;
    
        @Like
        @Link("personalData.name")
        private String name;
    
        @From
        @Link("personalData.birthday")
        private Date birthdayFrom;
    
        @To
        @Link("personalData.birthday")
        private Date birthdayTo;
    
        @Link("personalData.gender")
        private Gender gender;
    
        @In
        @Link("accounts.type")
        private List<AccountType> ownedAccountTypes;
    
        @IsNull
        @Link("selectedAccount")
        private Boolean selectedAccountAbsent;

        ...
    }
```

Currently, 6 filter types are supported: ``equals`` 
(the default one, will be used if filtering annotation isn't specified explicitly), ``@In``, ``@Like``, 
``@IsNull``, ``@IsNotNull`` (checks ``is null`` or ``is not null`` accordingly) and 
``@From``/``@To`` pair. Filtering predicate can be negated by marking the field with ``@Not`` annotation.
Note: if property has ``null`` value, filtering is not applied.

See, 7 fields and no ``toPredicate()`` function! But for complex cases that could not be covered by these annotations
you still can provide your own predicate creation mechanism:

[ClientExtendableSpecificationDto](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/dto/ClientExtendableSpecificationDto.java):

```java

    @ClassLink(Client.class)
    public class ClientExtendableSpecificationDto implements Specs<Client> {
    
        @In
        @Link("personalData.name")
        private List<String> names;
    
        @Link("selectedAccount.state.amount")
        private BigDecimal amount;
    
        @Ignored
        private Boolean onlyAdults;
    
        @Override
        public Predicate buildPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            if (onlyAdults != null) {
                return cb.lessThanOrEqualTo(root.get("personalData").get("birthday"),
                    Date.from(ZonedDateTime.now().minusYears(18).toInstant()));
            }
            return cb.and();
        }

```

## JSF and EJB 3

Let's take a look how to use ``ClientSpecificationDto`` with JSF and EJB 3 by creating a simple ManagedBean:

```java

    @ManagedBean
    public class ViewClientsMB {
    
        @EJB
        private DtoRepo dtoRepo;
    
        private Sheet<Client> page;
        private ClientSpecificationDto filter = new ClientSpecificationDto();
        private PagingAndSorting paging = new PagingAndSorting(0, 20);
    
        @PostConstruct
        public void init() {
            page = dtoRepo.getAll(filter, paging.orderBy("uid"));
        }
        
        ...
    }

```

You have filtering, sorting and paging out-of-the-box. Simple, isn't it?

## Spring MVC

Let's see an example of ``@Controller`` that uses ``SpringProcessor`` for filtering and sorting:

```java

    @RestController
    @RequestMapping("/clients/")
    public class ClientController {
    
        @Autowired
        private SpringProcessor processor;
    
        @RequestMapping(value = "v1/", method = RequestMethod.POST)
        private Page<ClientDto> read(@RequestBody Map<String, Filter> filters, Pageable pageable) {
            return processor.dao(ClientDto.class).findAll(filters, pageable);
        }
        
        @RequestMapping(value = "v2/", method = RequestMethod.POST)
        private Page<ClientDto> read(@RequestBody ClientSpecificationDto filter, Pageable pageable) {
            return processor.dao(ClientSpecificationDto.class).findAll(filter, pageable, ClientDto.class);
        }
    }
```

You can send POST requests to ``http://{your_url}/clients/v1/``
with JSON filtering payload and receive JSON results.

Currently two types of filtering are available. 
The one with ``ClientSpecificationDto`` (``v2/``) is almost identical to 
previous JSF + EJB section, despite the fact you're using Spring's ``Pageable`` and ``Page`` classes instead of LCL's 
``PagingAndSorting`` and ``Sheet``. 

Other type (using ``ClientDto`` - ``v1/``) is more dynamic: 
you can use not only properties specified directly in ``ClientDto``, but even their properties using dot-path.
For example you can send

```json

    {
        "name": {
            "$type": "like",
            "value": "Yurii"
        },
        "selectedAccount.amount": {
            "$type": "range",
            "fromExclusive": 999
        },
        "selectedAccount.type": {
            "value": "SAVINGS",
            "negated": true
        }
    }
```

to ``localhost:8761/clients/v1/?page=0&size=40&sort=selectedAccount.amount,asc`` and receive first 40 users
filtered by name (each must contains 'Yurii'), primary account's amount (starting from 999 excluding), 
account's type (not ``SAVINGS``) and ordered by amount of their primary banking account:

```json

    {
        "content": [
            {
                  "uid": 2,
                  "login": "y.m.smirnoff@gmail.com",
                  "name": "Yurii",
                  "birthday": 1454464414402,
                  "gender": "M",
                  "selectedAccount": {
                        "number": "749bcc7d-d80f-452a-9694-c1d37bba7700",
                        "amount": 1000,
                        "currency": "UAH",
                        "type": "CHECKING"
                  },
                  "accounts": [
                        {
                              "number": "749bcc7d-d80f-452a-9694-c1d37bba7700",
                              "amount": 1000,
                              "currency": "UAH",
                              "type": "CHECKING"
                        },
                        {
                              "number": "b74294e4-f55e-494a-a91e-b07f49ca2379",
                              "amount": 1000000,
                              "currency": "USD",
                              "type": "SAVINGS"
                        }
                  ],
                  "leadName": "John",
                  "managerName": null
            }
        ],
        "totalPages": 1,
        "last": true,
        "totalElements": 1,
        "size": 20,
        "number": 0,
        "first": true,
        "sort": [
            {
                  "direction": "ASC",
                  "property": "name",
                  "ignoreCase": false,
                  "nullHandling": "NATIVE",
                  "ascending": true
            },
            {
                  "direction": "ASC",
                  "property": "login",
                  "ignoreCase": false,
                  "nullHandling": "NATIVE",
                  "ascending": true
            },
            {
                  "direction": "ASC",
                  "property": "selectedAccount.amount",
                  "ignoreCase": false,
                  "nullHandling": "NATIVE",
                  "ascending": true
            }
        ],
        "numberOfElements": 1
    }
```

Again, 5 filter ``$type``s are supported: ``equals`` 
(the default one, will be taken if ``$type`` isn't specified explicitly), ``in``, ``like``, ``null`` and ``range``. 
Filter options are next:

- no essential options for ``null`` filter;
- ``value`` option for ``equals`` filter;
- ``value`` and ``useLowerCase`` options for ``like`` filter;
- ``values`` option for ``in`` filter (expecting array here);
- ``from``, ``fromExclusive``, ``to`` and ``toExclusive`` options for ``range`` filter.

Additionally, each filter (incl. ``null``) has ``negated`` option (non-mandatory, ``false`` by default) 
to invert filtering predicate.

# DTO <-> Entity transformations usage
## Creating a new Entities from DTOs

Besides DTOs for user viewing you also may need to create DTOs for typical create-update pages like user registration 
and profile editing.

[NewClientDto](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/dto/NewClientDto.java):

```java

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
        
        ...
        
    }
```

Let's assume that such data sent from frontend is enough for registering, but also you need to provide some 
client's property, for example auto-generated password, that isn't included into DTO. And after saving a new client 
you want to add a new ``Account`` for it, convert ``Client`` into our ``ClientDto`` and 
send it back to frontend. It all can be done like this:

### Spring MVC

```java

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

```

Here in ``loginData.password`` you specify the same property path, like in ``@Link`` annotation. Notice that 
``dao.repo()`` returns you ``ClientDao`` repository, you don't need to @Autowire it. 
If you don't need any predefined properties, you just can call ``dao.createAndSave(dto)`` without second argument.
If you don't need to save your Entity just after constructing it from DTO, you can call ``dao.create(dto, predefined)``.

### JSF + EJB 3

```java

    @EJB
    private DtoProcessor processor;
    @PersistenceContext
    private EntityManager em;

    private NewClientDto dto;
    
    ...
    
    public void createClient() {
        Map<String, Object> predefined = new HashMap<>();
        predefined.put("loginData.password", "password");
        
        Client client = processor.create(dto, predefined);
        em.persist(client);

        Account account = new Account("USD", AccountType.CHECKING, new AccountState(BigDecimal.ZERO), new Date());
        em.persist(account);

        client.setSelectedAccount(account);
        em.persist(client);
    }

```

## Updating an existing entities using DTOs

Now let's see profile updating.

[UpdateClientDto](https://github.com/SmirnoffYM/lcl/blob/master/lcl-spring-examples/src/main/java/com/habds/lcl/examples/dto/UpdateClientDto.java):

```java

    @ClassLink(Client.class)
    public class UpdateClientDto {
    
        @Link("personalData.name")
        private String name;
        @Link("personalData.birthday")
        private Date birthday;
        @Link("personalData.gender")
        private Gender gender;
    
        @Link("selectedAccount.number")
        private String selectedAccount;
                
        ...
        
    }
```

Here's other common scenario - you want to change some ``@OneToOne`` or ``@ManyToOne`` relation 
(``selectedAccount`` in this case). LCL knows, that ``selectedAccount.number`` points to ``@Id``-marked
property of ``@Entity``-marked ``Account``, thus decides to fetch an existing record using 
``EntityManager#find(Class<T> entityClass, Object primaryKey)`` / ``CrudRepository#findOne(T id)`` method 
and set it into ``Client`` object.

### Spring MVC

```java

    @RequestMapping(value = "{client}/", method = RequestMethod.POST)
    private ClientDto update(@PathVariable Client client, @RequestBody UpdateClientDto dto) {
        JpaDao<Client, UpdateClientDto> dao = processor.dao(UpdateClientDto.class);

        return processor.process(dao.updateAndSave(client, dto), ClientDto.class);
    }

```

You also can pass ``predefined`` properties calling ``dao.updateAndSave(client, dto, predefined)`` 
or perform update-without-saving by 
calling ``dao.update(client, dto)`` or ``dao.update(client, dto, predefined)`` methods, 
just like with ``create`` case.

### JSF + EJB 3

```java

    @EJB
    private DtoProcessor processor;
    @PersistenceContext
    private EntityManager em;

    private Client client;
    private UpdateClientDto dto;
    
    ...
    
    public void updateClient() {
        client = processor.merge(client, dto);
        em.merge(client);
    }

```