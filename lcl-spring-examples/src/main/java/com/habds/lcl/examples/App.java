package com.habds.lcl.examples;

import com.habds.lcl.core.data.filter.Filter;
import com.habds.lcl.spring.FilterDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Spring MVC with LCL example
 *
 * @author Yurii Smyrnov
 * @version 1
 * @since 1/31/16 12:39 AM
 */
@EnableWebMvc
@SpringBootApplication
public class App extends WebMvcConfigurerAdapter {

    @Autowired
    private Jackson2ObjectMapperBuilder jacksonBuilder;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
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
