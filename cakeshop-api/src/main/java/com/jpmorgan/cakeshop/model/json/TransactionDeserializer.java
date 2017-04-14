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
import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class TransactionDeserializer extends JsonDeserializer<TransPostJsonResquest> {

    @Override
    public TransPostJsonResquest deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {

        TransPostJsonResquest request = new TransPostJsonResquest();
        JsonNode node = jp.getCodec().readTree(jp);

        if (null != node.get("privateFor")) {
            JsonNode privateForNode = node.get("privateFor");
            List<String> privateFor;
            if (privateForNode.isArray()) {
                privateFor = Lists.newArrayList();
                for (Iterator<JsonNode> iter = privateForNode.elements(); iter.hasNext();) {
                    privateFor.add(iter.next().asText());
                }
            } else {
                privateFor = Lists.newArrayList(node.get("args").textValue());
            }
            request.setPrivateFor(privateFor);
        }

        if (null != node.get("ids")) {
            JsonNode idsNode = node.get("ids");
            List<String> ids;
            if (idsNode.isArray()) {
                ids = Lists.newArrayList();
                for (Iterator<JsonNode> iter = idsNode.elements(); iter.hasNext();) {
                    ids.add(iter.next().asText());
                }
            } else {
                ids = Lists.newArrayList(node.get("ids").textValue());
            }
            request.setIds(ids);
        }

        if (null != node.get("id")) {
            request.setId(node.get("id").textValue());
        }

        if (null != node.get("from")) {
            request.setFrom(node.get("from").textValue());
        }

        if (null != node.get("to")) {
            request.setTo(node.get("to").textValue());
        }

        if (null != node.get("data")) {
            request.setData(node.get("data").textValue());
        }

        if (null != node.get("privateFrom")) {
            request.setPrivateFrom(node.get("privateFrom").textValue());
        }

        return request;
    }

}
