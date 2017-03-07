/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.model.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import org.springframework.util.StringUtils;

/**
 *
 * @author I629630
 */
public class TransactionDeserializer extends JsonDeserializer<TransPostJsonResquest> {

    @Override
    public TransPostJsonResquest deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {

        TransPostJsonResquest request = new TransPostJsonResquest();
        JsonNode node = jp.getCodec().readTree(jp);

        if (null != node.get("privateFor")) {
            String privateFor = node.get("privateFor").textValue();
            if (StringUtils.isEmpty(privateFor)) {
                request.setPrivateFor(null);
            } else {
                request.setPrivateFor(node.get("privateFor").findValuesAsText(node.get("privateFor").asText()));
            }
        }

        if (null != node.get("ids")) {
            String ids = node.get("ids").textValue();
            if (StringUtils.isEmpty(ids)) {
                request.setIds(null);
            } else {
                request.setIds(node.get("ids").findValuesAsText(node.get("ids").asText()));
            }
        }

        if (null != node.get("id")) {
            request.setTo(node.get("id").textValue());
        }

        if (null != node.get("from")) {
            request.setFrom(node.get("from").textValue());
        }

        if (null != node.get("to")) {
            request.setTo(node.get("to").textValue());
        }

        if (null != node.get("data")) {
            request.setTo(node.get("data").textValue());
        }

        if (null != node.get("privateFrom")) {
            request.setPrivateFrom(node.get("privateFrom").textValue());
        }

        return request;
    }

}
