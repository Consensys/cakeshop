package com.jpmorgan.cakeshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.data.rest.configuration.SpringDataRestConfiguration;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
@Import(SpringDataRestConfiguration.class)
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jpmorgan.cakeshop"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Cakeshop REST API",
                "Sets of APIs to manage ethereum.",
                "API 1.0",
                "Terms of service",
                ApiInfo.DEFAULT_CONTACT.getEmail(),
                "License of API",
                "API license URL");
        return apiInfo;
    }

}
