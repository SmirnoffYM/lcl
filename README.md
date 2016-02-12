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

So you want send DTOs to your frontend (for example rich AngularJS application) with possibility to
filter and sort them by any of their properties:

```java

    public class ClientDto {
    
        private long uid;
        private String login;
        private String name;
        private Date birthday;
    
        private Gender gender;
    
        private AccountDto selectedAccount;
        private Set<AccountDto> accounts;
    
        private String leadName;
        private String managerName;

        ...
    }
```

Using Spring you probably write your own filter object (something like 
``class ClientSpecification implements Specification<Client>``) 
that holds all filtered fields and Predicate builder function, 
set up ``Sort`` object, then call ``clientDAO.findAll(specification, pageable)`` and 
manually map the resulting Page<Client> into Page<ClientDto> 
setting up all 9 properties like this: ``dto.setLogin(client.getLoginData().getEmail())``. Too complex as for me.

# Filtering and sorting with lcl-spring

Now let's see an example how to deal with it using *lcl-spring*. 
First of all, add ``lcl-spring`` into your project. Here's Maven dependency:

```xml

    <dependency>
        <groupId>com.habds</groupId>
        <artifactId>lcl-spring</artifactId>
        <version>0.9.1.RELEASE</version>
    </dependency>
```

Now, equip your ``ClientDto`` with ``@ClassLink`` and ``@Link`` annotations.

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
    
        @Link("personalData.gender")
        private Gender gender;
    
        private AccountDto selectedAccount;
        @Contains(AccountDto.class)
        private Set<AccountDto> accounts = new LinkedHashSet<>();
    
        @Link("lead.personalData.name")
        private String leadName;
        @Link("manager.personalData.name")
        private String managerName;

        ...
    }
```

Here ``@Link("loginData.email") private String login;`` means that DTO's ``login`` property value will be taken from 
``email`` field of ``LoginData`` embeddable object belonging to ``User`` (superclass of ``Client``).

Next, set up Jackson to allow deserialization of incoming filters and register ``SpringProcessor`` bean to deal 
with transforming, filtering and sorting.

```java

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
```

After that let's create a ``@Controller``:

```java

    @RestController
    @RequestMapping("/clients/")
    public class ClientController {
    
        @Autowired
        private SpringProcessor processor;
    
        @RequestMapping(method = RequestMethod.POST)
        private Page<ClientDto> read(@RequestBody Map<String, Filter> filters, Pageable pageable) {
            return processor.dao(ClientDto.class).findAll(filters, pageable);
        }
    }
```

That's all! Spring Processor understands that he needs to look up for ``Client`` entities, because ``ClientDto`` is 
``@ClassLink``ed with it. Now you can send POST requests to ``http://{your_url}/clients/`` with JSON filtering payload 
and receive JSON results.

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

to ``localhost:8761/clients/?page=0&size=40&sort=selectedAccount.amount,asc`` and receive first 40 users
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

Currently, 4 filter ``$type``s are supported: ``equals`` 
(the default one, will be taken if ``$type`` isn't specified explicitly), ``in``, ``like`` and ``range``. 
Filter options are next:

- ``value`` option for ``equals`` and ``like`` filter;
- ``values`` option for ``in`` filter (expecting array here);
- ``from``, ``fromExclusive``, ``to`` and ``toExclusive`` options for ``range`` filter.

Additionally, each filter has ``negated`` option (non-mandatory, ``false`` by default) to invert filtering predicate.

Paging and sorting request/response parameters (Sort and Pageable) remain the same as before - please refer to 
Spring Data documentation for more information.

# DTO <-> Entity transformations with lcl-spring

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
(``selectedAccount`` in this case). ``lcl-spring`` knows, that ``selectedAccount.number`` points to ``@Id``-marked
property of ``@Entity``-marked ``Account``, thus decides to fetch an existing record using 
``CrudRepository#findOne(T id)`` method and set it into ``Client`` object:

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
