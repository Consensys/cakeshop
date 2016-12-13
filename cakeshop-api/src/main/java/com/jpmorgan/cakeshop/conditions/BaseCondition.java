package com.jpmorgan.cakeshop.conditions;

import org.springframework.context.annotation.ConfigurationCondition;

/**
 *
 * @author Michael Kazansky
 */
public abstract class BaseCondition implements ConfigurationCondition {

    public final String ORACLE = "oracle";
    public final String MYSQL = "mysql";
    public final String POSTGRES = "postgres";
    public final String HSQL = "hsqldb";

    public String databaseName = System.getProperty("cakeshop.database.vendor");

}
