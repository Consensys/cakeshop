package com.jpmorgan.cakeshop.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class ImmutableRestTemplate extends RestTemplate {

    private final List<HttpMessageConverter<?>> immutableMessageConverters;

    public ImmutableRestTemplate() {
        super();
        this.immutableMessageConverters = ImmutableList.copyOf(super.getMessageConverters());
    }

    public ImmutableRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(requestFactory);
        this.immutableMessageConverters = ImmutableList.copyOf(super.getMessageConverters());
    }

    public ImmutableRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
        this.immutableMessageConverters = ImmutableList.copyOf(super.getMessageConverters());
    }

    @Override
    public List<HttpMessageConverter<?>> getMessageConverters() {
        return immutableMessageConverters;
    }

}
