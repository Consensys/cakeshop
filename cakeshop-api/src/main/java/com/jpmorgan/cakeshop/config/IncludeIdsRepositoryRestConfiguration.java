package com.jpmorgan.cakeshop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;

@Configuration
public class IncludeIdsRepositoryRestConfiguration extends RepositoryRestConfigurerAdapter {

    @Autowired
    private EntityManager entityManager;

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(entityManager
            .getMetamodel()
            .getEntities()
            .stream()
            .map(Type::getJavaType)
            .toArray(Class[]::new));

    }
}
