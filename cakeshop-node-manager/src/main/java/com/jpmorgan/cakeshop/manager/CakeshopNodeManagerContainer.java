package com.jpmorgan.cakeshop.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@Profile("container")
public class CakeshopNodeManagerContainer extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(APPLICATION_CLASS, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(APPLICATION_CLASS);
    }

    private static final Class<CakeshopNodeManagerContainer> APPLICATION_CLASS = CakeshopNodeManagerContainer.class;

}
