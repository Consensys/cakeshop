package com.jpmorgan.cakeshop.config.rdbms;

import com.jpmorgan.cakeshop.conditions.HsqlDataSourceConditon;
import com.jpmorgan.cakeshop.util.FileUtils;

import java.sql.Driver;
import java.util.Properties;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Conditional(HsqlDataSourceConditon.class)
@Configuration
@EnableTransactionManagement
@ComponentScan({ "com.jpmorgan.cakeshop.model" })
public class EmbeddedDbDataSourceConfig implements ApplicationContextAware {

    protected static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EmbeddedDbDataSourceConfig.class);

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Value("${hibernate.hbm2ddl.auto:update}")
    private String hibernateAuto;

    @Value("${hibernate.dialect:org.hibernate.dialect.HSQLDialect}")
    private String hibernateDialect;

    @Value("${hibernate.jdbc.batch_size:20}")
    private String hibernateBatchSize;

    private ApplicationContext applicationContext;

    // Embedded DB instance, IF we are using one
    private EmbeddedDatabase embeddedDb;

    @Bean
    @Autowired
    public HibernateTemplate hibernateTemplate(SessionFactory sessionFactory) {
        return new HibernateTemplate(sessionFactory);
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
       LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
       sessionFactory.setDataSource(applicationContext.getBean(DataSource.class));
       sessionFactory.setPackagesToScan(new String[] { "com.jpmorgan.cakeshop.model" });
       sessionFactory.setHibernateProperties(hibernateProperties());

       return sessionFactory;
    }

    @Bean
    @Autowired
    public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
       HibernateTransactionManager txManager = new HibernateTransactionManager();
       txManager.setSessionFactory(sessionFactory);
       return txManager;
    }

    @SuppressWarnings("serial")
    Properties hibernateProperties() {
        return new Properties() {
            {
                setProperty("hibernate.jdbc.batch_size", hibernateBatchSize);
                setProperty("hibernate.hbm2ddl.auto", hibernateAuto);
                setProperty("hibernate.dialect", hibernateDialect);
                setProperty("hibernate.default_schema", "PUBLIC");
//                setProperty("hibernate.globally_quoted_identifiers", "true");
            }
        };
    }

    public DataSource createDataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(org.hsqldb.jdbcDriver.class);
        dataSource.setUrl("jdbc:hsqldb:file:" + getDbStoragePath() + ";hsqldb.default_table_type=cached");
        dataSource.setUsername("sdk");
        dataSource.setPassword("sdk");
        return dataSource;
    }

    public String getDbStoragePath() {
       return FileUtils.expandPath(CONFIG_ROOT, "db", "sdk");
    }

    @Bean(name="hsql")
    @Order(0)
    public DataSource startDb() {
        LOG.debug("USING Embedded HSQL DB");
        DataSourceFactory dataSourceFactory = new DataSourceFactory() {
            @Override
            public DataSource getDataSource() {
                return createDataSource();
            }

            @Override
            public ConnectionProperties getConnectionProperties() {
                return new ConnectionProperties() {
                    @Override
                    public void setUsername(String username) {
                    }
                    @Override
                    public void setUrl(String url) {
                    }
                    @Override
                    public void setPassword(String password) {
                    }
                    @Override
                    public void setDriverClass(Class<? extends Driver> driverClass) {
                    }
                };
            }
        };

        this.embeddedDb = new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setType(EmbeddedDatabaseType.HSQL)
            .setScriptEncoding("UTF-8")
            .setDataSourceFactory(dataSourceFactory)
            .ignoreFailedDrops(true)
            .build();

        return this.embeddedDb;
    }

    @PreDestroy
    public void shutdownDb() {
        if (this.embeddedDb != null) {
            this.embeddedDb.shutdown();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

    }

}
