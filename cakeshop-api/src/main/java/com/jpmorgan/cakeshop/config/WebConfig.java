package com.jpmorgan.cakeshop.config;

import java.io.FileInputStream;
import java.util.Properties;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import okhttp3.OkHttpClient;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import org.springframework.beans.factory.annotation.Value;
import com.jpmorgan.cakeshop.util.FileUtils;

/**
 *
 * @author Michael Kazansky
 */
@Configuration
@EnableScheduling
public class WebConfig extends WebMvcConfigurerAdapter {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebConfig.class);

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private Environment env;

    @Autowired
    private OkHttpClient okHttpClient;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(createMvcAsyncExecutor());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String configFile = FileUtils.expandPath(CONFIG_ROOT, "application.properties");
        Properties props = new Properties();
        try {
        	props.load(new FileInputStream(configFile));
        } catch (Exception e) { }
       
        if (Boolean.valueOf(env.getProperty("geth.cors.enabled"))) {
            registry.addMapping("/**")
                    .allowedOrigins(env.getProperty("geth.cors.url"));
        } else if (Boolean.valueOf(props.getProperty("geth.cors.enabled"))) {
            registry.addMapping("/**")
            .allowedOrigins(props.getProperty("geth.cors.url"));
        }
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // Enable DefaultServlet handler for static resources at /**
        configurer.enable();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public ServletContextTemplateResolver templateResolver() {
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver();
        templateResolver.setCacheable(false);
        templateResolver.setTemplateMode("HTML5");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setPrefix("/resources/");
        templateResolver.setSuffix(".html");

        return templateResolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }

    @Bean
    public ViewResolver getViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setOrder(1);
        resolver.setViewNames(new String[]{"*.html"});
        resolver.setTemplateEngine(templateEngine());
        return resolver;
    }

    @PreDestroy
    public void shutdown() {
        okHttpClient.connectionPool().evictAll();
    }

    /**
     * Thread pool used by Spring WebMVC async 'Callable'
     * https://spring.io/blog/2012/05/10/spring-mvc-3-2-preview-making-a-controller-method-asynchronous/
     *
     * @return
     */
    @Bean(name = "asyncTaskExecutor")
    public AsyncTaskExecutor createMvcAsyncExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setBeanName("asyncTaskExecutor");
        exec.setCorePoolSize(Integer.valueOf(env.getProperty("cakeshop.mvc.async.pool.threads.core")));
        exec.setMaxPoolSize(Integer.valueOf(env.getProperty("cakeshop.mvc.async.pool.threads.max")));
        exec.setQueueCapacity(Integer.valueOf(env.getProperty("cakeshop.mvc.async.pool.queue.max")));
        exec.setThreadNamePrefix("WebMvc-");
        exec.afterPropertiesSet();
        return exec;
    }
}
