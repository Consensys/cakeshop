package com.jpmorgan.cakeshop.test.config;

import com.jpmorgan.cakeshop.config.AppConfig;
import com.jpmorgan.cakeshop.config.SpringBootApplication;
import com.jpmorgan.cakeshop.config.SwaggerConfig;
import com.jpmorgan.cakeshop.config.WebAppInit;
import com.jpmorgan.cakeshop.config.WebConfig;
import com.jpmorgan.cakeshop.db.BlockScanner;
import com.jpmorgan.cakeshop.test.TestBlockScanner;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeClass;

@Configuration
@ComponentScan(basePackages = "com.jpmorgan.cakeshop",
        excludeFilters = {
            @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                    value = {SpringBootApplication.class, WebConfig.class, WebAppInit.class,
                        BlockScanner.class, SwaggerConfig.class}
            )
        }
)
@ActiveProfiles("test")
@Order(1)
@EnableAsync
public class TestAppConfig implements EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(TestAppConfig.class);
    private Environment env;

    @BeforeClass
    public static void setUp() {
        System.setProperty("spring.profiles.active", "test");
        System.setProperty("cakeshop.database.vendor", "hsqldb");
    }

    @Bean
    @Profile("test")
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        AppConfig appConfig = new AppConfig();
        return appConfig.createPropConfigurer(TempFileManager.getTempPath());
    }

    @Bean(name = "asyncExecutor")
    public Executor getAsyncExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean
    public BlockScanner createBlockScanner() {
        return new TestBlockScanner();
    }

    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    @Override
    public void setEnvironment(Environment e) {
        this.env = e;
    }
}
