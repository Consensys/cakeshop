package com.jpmorgan.cakeshop.manager.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class AppConfig extends WebMvcConfigurerAdapter {

    @Bean
    public static PropertySourcesPlaceholderConfigurer
            propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter jacksonMessageConverter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = jacksonMessageConverter.getObjectMapper();
        objectMapper.addMixIn(APIResponse.class, IgnoreAPIresponse.class);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        converters.add(jacksonMessageConverter);
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    abstract class IgnoreAPIresponse {

        @JsonIgnore
        public abstract Boolean getData();
    }
}
