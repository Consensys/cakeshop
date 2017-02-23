package com.jpmorgan.cakeshop.config;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import okhttp3.OkHttpClient;
import org.springframework.web.servlet.ViewResolver;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

/**
 *
 * @author Michael Kazansky
 */
@Configuration
@EnableScheduling
public class WebConfig extends WebMvcConfigurerAdapter {
    
    @Autowired
    private Environment env;
    
    @Autowired
    private RequestMappingHandlerAdapter adapter;
    
    @Autowired
    private OkHttpClient okHttpClient;
    
    @PostConstruct
    public void prioritizeCustomArgumentMethodHandlers() {
        // existing resolvers
        List<HandlerMethodArgumentResolver> argumentResolvers
                = new ArrayList<>(adapter.getArgumentResolvers());

        // add our resolvers at pos 0
        List<HandlerMethodArgumentResolver> customResolvers
                = adapter.getCustomArgumentResolvers();

        // empty and re-add our custom list
        argumentResolvers.removeAll(customResolvers);
        argumentResolvers.addAll(0, customResolvers);
        
        adapter.setArgumentResolvers(argumentResolvers);
    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.add(new JsonMethodArgumentResolver());
    }
    
    @Override
    
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(createMvcAsyncExecutor());
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (Boolean.valueOf(env.getProperty("geth.cors.enabled:true"))) {
            registry.addMapping("/**")
                    .allowedOrigins(env.getProperty("geth.cors.url"))
                    .allowedMethods("POST");
        }
    }
    
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // Enable DefaultServlet handler for static resources at /**
        configurer.enable();
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
