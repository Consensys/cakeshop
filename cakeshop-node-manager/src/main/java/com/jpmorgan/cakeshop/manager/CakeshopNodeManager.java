package com.jpmorgan.cakeshop.manager;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

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
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory
                = new TomcatEmbeddedServletContainerFactory();
        factory.setSessionTimeout(15, TimeUnit.MINUTES);
        return factory;
    }
}
