package com.jpmorgan.cakeshop.config;

import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author Michael Kazansky
 */
@Configuration
@EnableScheduling
@Slf4j
public class WebConfig implements WebMvcConfigurer {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebConfig.class);

    @Value("${cakeshop.mvc.async.pool.threads.core:250}")
    private Integer corePoolSize;

    @Value("${cakeshop.mvc.async.pool.threads.max:1000}")
    private Integer maxPoolSize;

    @Value("${cakeshop.mvc.async.pool.queue.max:2000}")
    private Integer queueCapacity;

    @Autowired
    private OkHttpClient okHttpClient;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(createMvcAsyncExecutor());
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
        LOG.info("async task pool thread core {}", corePoolSize);
        exec.setCorePoolSize(corePoolSize);
        exec.setMaxPoolSize(maxPoolSize);
        exec.setQueueCapacity(queueCapacity);
        exec.setThreadNamePrefix("WebMvc-");
        exec.afterPropertiesSet();
        return exec;
    }
}
