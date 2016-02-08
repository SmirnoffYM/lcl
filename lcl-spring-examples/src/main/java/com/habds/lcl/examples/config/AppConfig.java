package com.habds.lcl.examples.config;

import com.habds.lcl.core.data.EntityManagerRepository;
import com.habds.lcl.core.processor.Processor;
import com.habds.lcl.spring.SpringProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.EntityManager;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.habds.lcl.examples.persistence.dao")
@EntityScan(basePackages = "com.habds.lcl.examples.persistence.bo")
public class AppConfig {

    @Bean
    public SpringProcessor processor() {
        return (SpringProcessor) new SpringProcessor().add("com.habds.lcl.examples").configure();
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    public EntityManagerRepository emRepository(Processor processor, EntityManager em) {
        return new EntityManagerRepository(processor, em);
    }
}
