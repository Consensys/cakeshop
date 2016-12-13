package com.jpmorgan.cakeshop.config;

import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.SortedProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jmx.export.annotation.AnnotationMBeanExporter;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AppConfig implements AsyncConfigurer, EnvironmentAware {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    public static final String CONFIG_FILE = "application.properties";

    private Environment env;

    @Override
    public  void setEnvironment(Environment e) {
        this.env = e;
    }


    /**
     * Return the configured environment name
     *
     * Search order:
     * - ETH_ENV environment variable
     * - eth.environment system property (-Deth.environment param)
     * - Default to 'local' if none found
     *
     * @return Environment name
     */
    public  String getEnv() {
        //String env = getProp("ETH_ENV", "eth.environment");
        if (null == env) {
            // FIXME only default to local based on a flag passed down from maven build?
            LOG.warn("spring.profiles.active is not set");
        }
        List <String> profiles = ListUtils.selectRejected(Arrays.asList(env.getActiveProfiles()), new Predicate <String>() {
            @Override
            public boolean evaluate(String profile) {
                return profile.equalsIgnoreCase("container") || profile.equalsIgnoreCase("spring-boot");
            }
        });
        return profiles.get(0);
    }

    /**
     * Return the configured config location
     *
     * Search order:
     * - ETH_CONFIG environment variable
     * - eth.config.dir system property (-Deth.config.dir param)
     * - Detect tomcat (container) root relative to classpath
     *
     * @return
     */
    public  String getConfigPath() {
        String configPath = getProp("ETH_CONFIG", "eth.config.dir");
        if (!StringUtils.isBlank(configPath)) {
            return FileUtils.expandPath(configPath, getEnv());
        }

        String webappRoot = FileUtils.expandPath(FileUtils.getClasspathPath(""), "..", "..");
        String tomcatRoot = FileUtils.expandPath(webappRoot, "..", "..");

        // migrate conf dir to new name
        File oldPath = new File(FileUtils.expandPath(tomcatRoot, "data", "enterprise-ethereum"));
        File newPath = new File(FileUtils.expandPath(tomcatRoot, "data", "cakeshop"));
        if (oldPath.exists() && !newPath.exists()) {
            oldPath.renameTo(newPath);
        }

        return FileUtils.expandPath(tomcatRoot, "data", "cakeshop", getEnv());
    }

    private static String getProp(String env, String java) {
        String str = System.getenv(env);
        if (StringUtils.isBlank(str)) {
            str = System.getProperty(java);
        }
        return str;
    }

    @Bean
//    @Profile("container")
    public  PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() throws IOException {
        return createPropConfigurer(env, getConfigPath());
    }

    public  String getVendorConfigFile() {
        return "config".concat(File.separator).concat("application.properties");
    }

    public  String getVendorEnvConfigFile() {
        return "config".concat(File.separator).concat("application-").concat(getEnv()).concat(".properties");
    }

    public  void initVendorConfig(File configFile) throws IOException {
        // copy default file
        String path = FileUtils.getClasspathPath(getVendorEnvConfigFile()).toString();
        LOG.info("Initializing new config from " + path);

        // defaults + env defaults
        Properties mergedProps = new Properties();
        mergedProps.load(FileUtils.getClasspathStream(getVendorConfigFile()));
        mergedProps.load(FileUtils.getClasspathStream(getVendorEnvConfigFile()));

        SortedProperties.store(mergedProps, new FileOutputStream(configFile));
    }

    public  PropertySourcesPlaceholderConfigurer createPropConfigurer(Environment env,
            String configDir) throws IOException {

        if (null == env) {
            throw new IOException("ENV var 'spring.profiles.active' not set; unable to load config");
        }

        LOG.info("eth.environment=" + env);
        LOG.info("eth.config.dir=" + configDir);

        File configPath = new File(configDir);
        File configFile = new File(configPath.getPath() + File.separator + CONFIG_FILE);

        if (!configPath.exists() || !configFile.exists()) {
            LOG.debug("Config dir does not exist, will init");

            configPath.mkdirs();
            if (!configPath.exists()) {
               throw new IOException("Unable to create config dir: " + configPath.getAbsolutePath());
            }

            initVendorConfig(configFile);

        } else {
            Properties mergedProps = new Properties();
            mergedProps.load(FileUtils.getClasspathStream(getVendorEnvConfigFile()));
            mergedProps.load(new FileInputStream(configFile)); // overwrite vendor props with our configs
            SortedProperties.store(mergedProps, new FileOutputStream(configFile));
        }

        // Finally create the configurer and return it
        Properties localProps = new Properties();
        localProps.setProperty("config.path", configPath.getPath());

        PropertySourcesPlaceholderConfigurer propConfig = new PropertySourcesPlaceholderConfigurer();
        propConfig.setLocation(new FileSystemResource(configFile));
        propConfig.setProperties(localProps);

        return propConfig;
    }

    @Bean(name="asyncExecutor")
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


}
