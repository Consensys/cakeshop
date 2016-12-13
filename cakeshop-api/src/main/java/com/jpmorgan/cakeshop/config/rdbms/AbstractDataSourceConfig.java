package com.jpmorgan.cakeshop.config.rdbms;


import com.jpmorgan.cakeshop.util.StringUtils;

import java.util.Properties;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.lookup.JndiDataSourceLookup;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = {"com.jpmorgan.cakeshop.model"})
public abstract class AbstractDataSourceConfig implements ApplicationContextAware {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AbstractDataSourceConfig.class);
    private final String JNDI_NAME_PROP = "cakeshop.jndi.name";
    private final String JNDI_NAME = System.getProperty(JNDI_NAME_PROP);

    protected final String JDBC_URL = "cakeshop.jdbc.url";
    protected final String JDBC_USER = "cakeshop.jdbc.user";
    protected final String JDBC_PASS =  "cakeshop.jdbc.pass";
    public static final String JDBC_BATCH_SIZE = "cakeshop.hibernate.jdbc.batch_size";
    protected final String HBM_2DDL_AUTO = "cakeshop.hibernate.hbm2ddl.auto";
    protected final String HIBERNATE_DIALECT = "cakeshop.hibernate.dialect";

    @Autowired
    protected Environment env;

    @Value("${config.path}")
    protected String CONFIG_ROOT;

    protected ApplicationContext applicationContext;


    @Bean
    public HibernateTemplate hibernateTemplate(SessionFactory sessionFactory) {
        return new HibernateTemplate(sessionFactory);
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() throws ClassNotFoundException, NamingException {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan(new String[]{"com.jpmorgan.cakeshop.model"});
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
            return  getSimpleDataSource();
        }
    }
}
