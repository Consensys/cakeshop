package com.jpmorgan.cakeshop.manager.config.db;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;

import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public abstract class AbstractDataSourceConfig implements ApplicationContextAware {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractDataSourceConfig.class);
    private final String JNDI_NAME_PROP = "nodemanager.jndi.name";
    private final String JNDI_NAME = System.getProperty(JNDI_NAME_PROP);

    protected final String JDBC_URL = "nodemanager.jdbc.url";
    protected final String JDBC_USER = "nodemanager.jdbc.user";
    protected final String JDBC_PASS = "nodemanager.jdbc.pass";
    protected final String HBM_2DDL_AUTO = "nodemanager.hibernate.hbm2ddl.auto";
    protected final String HIBERNATE_DIALECT = "nodemanager.hibernate.dialect";
    public static final String JDBC_BATCH_SIZE = "nodemanager.hibernate.jdbc.batch_size";

    @Autowired
    protected Environment env;

    protected ApplicationContext applicationContext;

    @Bean
    public HibernateTemplate hibernateTemplate(SessionFactory sessionFactory) {
        return new HibernateTemplate(sessionFactory);
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() throws ClassNotFoundException, NamingException {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan(new String[]{"com.jpmorgan.cakeshop.manager.db.entity"});
        sessionFactory.setHibernateProperties(hibernateProperties());

        return sessionFactory;
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) throws ClassNotFoundException, NamingException {
        HibernateTransactionManager txManager = new HibernateTransactionManager();
        txManager.setSessionFactory(sessionFactory);
        return txManager;
    }

    protected abstract Properties hibernateProperties();

    protected abstract DataSource getSimpleDataSource() throws ClassNotFoundException;

    @Bean
    public DataSource dataSource() throws ClassNotFoundException, NamingException {
        String jndiName = StringUtils.isNotBlank(JNDI_NAME) ? JNDI_NAME : env.getProperty(JNDI_NAME_PROP);
        if (StringUtils.isNotBlank(jndiName)) {
            final JndiDataSourceLookup dataSourceLookup = new JndiDataSourceLookup();
            dataSourceLookup.setResourceRef(true);
            DataSource dataSource = dataSourceLookup.getDataSource(jndiName);
            return dataSource;
        } else {
            return getSimpleDataSource();
        }
    }
}
