package com.jpmorgan.cakeshop.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.jpmorgan.cakeshop.controller"))
                .paths(Predicates.not(PathSelectors.regex("/login")))
                .paths(Predicates.not(PathSelectors.regex("/logout")))
                .paths(Predicates.not(PathSelectors.regex("/user")))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        ApiInfo apiInfo = new ApiInfo(
                "Cakeshop REST API",
                "Sets of APIs to manage ethereum.",
                "API 1.0",
                "Terms of service",
                ApiInfo.DEFAULT_CONTACT,
                "License of API",
                "API license URL");
        return apiInfo;
    }

}
