package com.jpmorgan.cakeshop.manager.dao;

import static com.jpmorgan.cakeshop.manager.config.db.AbstractDataSourceConfig.JDBC_BATCH_SIZE;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public abstract class BaseDAO {

    @Value(value = "${" + JDBC_BATCH_SIZE + "}:20")
    private String batchSize;

    @Autowired(required = false)
    private SessionFactory sessionFactory;

    protected final Integer BATCH_SIZE = StringUtils.isNotBlank(System.getProperty(JDBC_BATCH_SIZE))
            ? Integer.valueOf(System.getProperty(JDBC_BATCH_SIZE))
            : StringUtils.isNotBlank(batchSize) ? Integer.valueOf(batchSize)
            : 20;

    protected Session getCurrentSession() {
        Session session = null != sessionFactory ? sessionFactory.getCurrentSession() : null;
        return session;
    }

    public abstract void reset();

}
