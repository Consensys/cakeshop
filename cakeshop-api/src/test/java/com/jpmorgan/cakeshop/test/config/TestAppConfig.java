package com.jpmorgan.cakeshop.test.config;

import com.jpmorgan.cakeshop.CakeshopApplication;
import com.jpmorgan.cakeshop.config.SwaggerConfig;
import com.jpmorgan.cakeshop.config.WebAppInit;
import com.jpmorgan.cakeshop.config.WebConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeClass;

import java.util.concurrent.Executor;

@ComponentScan(basePackages = "com.jpmorgan.cakeshop",
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            value = {CakeshopApplication.class, WebConfig.class, WebAppInit.class, SwaggerConfig.class})
    }
)
@ActiveProfiles("test")
@PropertySource("classpath:config/application.properties")
@Order(1)
@EnableAsync
@SpringBootTest
@EnableJpaRepositories(basePackages = "com.jpmorgan.cakeshop.repo")
@EntityScan(basePackages = "com.jpmorgan.cakeshop.model")
@DataJpaTest
public class TestAppConfig implements EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(TestAppConfig.class);
    private Environment env;

    @BeforeClass
    public static void setUp() {
        System.setProperty("cakeshop.config.dir", TempFileManager.getTempPath());
        System.setProperty("spring.profiles.active", "test");
    }

    @Bean(name = "asyncExecutor")
    public Executor getAsyncExecutor() {
        return new SyncTaskExecutor();
    }

    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Override
    public void setEnvironment(Environment e) {
        this.env = e;
    }
}
