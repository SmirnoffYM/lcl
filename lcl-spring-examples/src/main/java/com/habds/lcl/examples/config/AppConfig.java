package com.habds.lcl.examples.config;

import com.habds.lcl.core.annotation.ClassLink;
import com.habds.lcl.core.data.EntityManagerRepository;
import com.habds.lcl.core.data.JpaLinkProcessor;
import com.habds.lcl.core.processor.impl.SimpleProcessor;
import com.habds.lcl.spring.ComponentScanUtils;
import com.habds.lcl.spring.SpringProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.EntityManager;
import java.util.stream.Collectors;

@Configuration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.habds.lcl.examples.persistence.dao")
@EntityScan(basePackages = "com.habds.lcl.examples.persistence.bo")
public class AppConfig {

    @Bean
    public SpringProcessor springProcessor() {
        return (SpringProcessor) new SpringProcessor().add("com.habds.lcl.examples").configure();
    }

    @Bean
    public SimpleProcessor processor(EntityManager em) {
        return new SimpleProcessor(new JpaLinkProcessor(em))
            .add(ComponentScanUtils.scan("com.habds.lcl.examples")
                .filter(c -> c.getAnnotation(ClassLink.class) != null)
                .collect(Collectors.toList()))
            .configure();
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    public EntityManagerRepository emRepository(SimpleProcessor processor, EntityManager em) {
        return new EntityManagerRepository(processor, em);
    }
}
