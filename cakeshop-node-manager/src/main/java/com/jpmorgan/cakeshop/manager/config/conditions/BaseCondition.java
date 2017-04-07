package com.jpmorgan.cakeshop.manager.config.conditions;

import org.springframework.context.annotation.ConfigurationCondition;

public abstract class BaseCondition implements ConfigurationCondition {

    public final String ORACLE = "oracle";
    public final String MYSQL = "mysql";
    public final String POSTGRES = "postgres";
    public final String HSQL = "hsqldb";

    public String databaseName = System.getProperty("nodemanager.database.vendor");

}
