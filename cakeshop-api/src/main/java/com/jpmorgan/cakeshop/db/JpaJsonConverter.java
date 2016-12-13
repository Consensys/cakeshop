package com.jpmorgan.cakeshop.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class JpaJsonConverter implements AttributeConverter<Object[], String> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JpaJsonConverter.class);

    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object[] meta) {
        try {
            return objectMapper.writeValueAsString(meta);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return null;
            // or throw an error
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] convertToEntityAttribute(String dbData) {
        try {
            List<Object> vals = (List<Object>) objectMapper.readValue(dbData, Object.class);
            return vals.toArray();
        } catch (IOException ex) {
            ex.printStackTrace();
            // logger.error("Unexpected IOEx decoding json from database: " +
            // dbData);
            return null;
        }
    }

}
