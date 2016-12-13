package com.jpmorgan.cakeshop.client.ws;

import com.google.common.collect.Lists;

import java.nio.charset.Charset;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

public class JsonMessageConverter extends AbstractMessageConverter {

    private final Charset defaultCharset;


    public JsonMessageConverter() {
        this(Charset.forName("UTF-8"));
    }

    public JsonMessageConverter(Charset defaultCharset) {
        super(Lists.newArrayList(MimeTypeUtils.APPLICATION_JSON, MimeTypeUtils.TEXT_PLAIN));
        this.defaultCharset = defaultCharset;
    }

	@Override
	protected boolean supports(Class<?> clazz) {
		return (String.class == clazz);
	}

    @Override
    protected Object convertFromInternal(Message<?> message, Class<?> targetClass, Object conversionHint) {
        Charset charset = getContentTypeCharset(getMimeType(message.getHeaders()));
        Object payload = message.getPayload();
        return (payload instanceof String ? payload : new String((byte[]) payload, charset));
    }

    @Override
    protected Object convertToInternal(Object payload, MessageHeaders headers, Object conversionHint) {
        if (byte[].class == getSerializedPayloadClass()) {
            Charset charset = getContentTypeCharset(getMimeType(headers));
            payload = ((String) payload).getBytes(charset);
        }
        return payload;
    }

    private Charset getContentTypeCharset(MimeType mimeType) {
        if (mimeType != null && mimeType.getCharSet() != null) {
            return mimeType.getCharSet();
        }
        else {
            return this.defaultCharset;
        }
    }

}
