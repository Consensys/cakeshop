package com.jpmorgan.cakeshop.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.SortedProperties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AppConfig implements AsyncConfigurer {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    @Primary
    @Bean
    public ObjectMapper jsonMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    @Bean(name = "asyncExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(10);
        exec.setMaxPoolSize(500);
        exec.setQueueCapacity(2000);
        exec.setThreadNamePrefix("cake-");
        exec.afterPropertiesSet();
        return exec;
    }

    @Bean
    public AnnotationMBeanExporter annotationMBeanExporter() {
        AnnotationMBeanExporter annotationMBeanExporter = new AnnotationMBeanExporter();
        annotationMBeanExporter.addExcludedBean("dataSource");
        return annotationMBeanExporter;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }

    public static void setSecurity(Properties properties) {
        Boolean securityEnabled
                = StringUtils.isNotBlank(System.getProperty("cakeshop.security.enabled"))
                ? Boolean.valueOf(System.getProperty("cakeshop.security.enabled"))
                : false;
        if (securityEnabled) {
            if (StringUtils.isNotBlank(properties.getProperty("management.security.enabled"))) {
                properties.remove("management.security.enabled");
                System.setProperty("management.security.enabled", "true");
            }
            if (StringUtils.isNotBlank(properties.getProperty("security.basic.enabled"))) {
                properties.remove("security.basic.enabled");
                System.setProperty("security.basic.enabled", "true");
            }
            if (StringUtils.isNotBlank(properties.getProperty("security.ignored"))) {
                System.setProperty("security.ignored", "");
                properties.remove("security.ignored");
            }

        } else {
            if (StringUtils.isBlank(properties.getProperty("management.security.enabled"))) {
                properties.setProperty("management.security.enabled", "false");
            }
            if (StringUtils.isBlank(properties.getProperty("security.basic.enabled"))) {
                properties.setProperty("security.basic.enabled", "false");
            }
            if (StringUtils.isBlank(properties.getProperty("security.ignored"))) {
                properties.setProperty("security.ignored", "/**");
            }
            LOG.warn("Authentication disabled.");
        }

    }

}
