package com.jpmorgan.cakeshop.config;

import com.jpmorgan.cakeshop.util.ImmutableRestTemplate;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

@Configuration
public class HttpClientConfig {

    @Value("${cakeshop.http.pool.size:500}")
    private Integer maxIdleSize;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(maxIdleSize, 1, TimeUnit.MINUTES))
                .build();
    }

    @Bean
    public ClientHttpRequestFactory httpRequestFactory(OkHttpClient okc) {
        return new OkHttp3ClientHttpRequestFactory(okc);
    }

    @Bean
    public RestTemplate createRestTemplate(ClientHttpRequestFactory httpRequestFactory) {
        return new ImmutableRestTemplate(httpRequestFactory);
    }

}
