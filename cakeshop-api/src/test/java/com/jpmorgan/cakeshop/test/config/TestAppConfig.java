package com.jpmorgan.cakeshop.test.config;

import com.jpmorgan.cakeshop.config.SpringBootApplication;
import com.jpmorgan.cakeshop.config.SwaggerConfig;
import com.jpmorgan.cakeshop.config.WebAppInit;
import com.jpmorgan.cakeshop.config.WebConfig;
import com.jpmorgan.cakeshop.db.BlockScanner;
import com.jpmorgan.cakeshop.test.TestBlockScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeClass;

import java.util.concurrent.Executor;

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
@PropertySource("classpath:config/application.properties")
@Order(1)
@EnableAsync
public class TestAppConfig implements EnvironmentAware {

    private static final Logger LOG = LoggerFactory.getLogger(TestAppConfig.class);
    private Environment env;

    @BeforeClass
    public static void setUp() {
        System.setProperty("cakeshop.config.dir", TempFileManager.getTempPath());
        System.setProperty("spring.profiles.active", "test");
        System.setProperty("cakeshop.database.vendor", "hsqldb");
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
