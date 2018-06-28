package com.jpmorgan.cakeshop.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
public class CakeshopNodeManager extends SpringBootServletInitializer {

    private static final Class<CakeshopNodeManager> APPLICATION_CLASS = CakeshopNodeManager.class;

    public static void main(String[] args) {
        SpringApplication.run(APPLICATION_CLASS, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(APPLICATION_CLASS);
    }

    @Bean
    @Profile("spring-boot")
      public ConfigurableServletWebServerFactory servletContainer(Session session) {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        session.setTimeout(Duration.ofMinutes(15));
        factory.setSession(session);
        return factory;
    }

}
