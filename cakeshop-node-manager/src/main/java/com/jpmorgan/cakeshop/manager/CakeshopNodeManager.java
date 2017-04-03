package com.jpmorgan.cakeshop.manager;

import java.util.concurrent.TimeUnit;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@Profile("spring-boot")
public class CakeshopNodeManager {

    public static void main(String[] args) {
        new SpringApplicationBuilder(CakeshopNodeManager.class).web(true).run(args);
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory
                = new TomcatEmbeddedServletContainerFactory();
        factory.setSessionTimeout(15, TimeUnit.MINUTES);
        return factory;
    }
}
