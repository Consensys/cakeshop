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
import com.jpmorgan.cakeshop.util.StringUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContractDeserializer extends JsonDeserializer<ContractPostJsonRequest> {

    @Override
    public ContractPostJsonRequest deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        ContractPostJsonRequest request = new ContractPostJsonRequest();
        JsonNode node = jp.getCodec().readTree(jp);

        if (null != node.get("privateFor")) {
            JsonNode privateForNode = node.get("privateFor");
            List<String> privateFor = null;
            
            if (privateForNode.isArray()) {
                privateFor = Lists.newArrayList();
                for (Iterator<JsonNode> iter = privateForNode.elements(); iter.hasNext();) {
                    privateFor.add(iter.next().asText());
                }
            } else {
                if (StringUtils.isNotBlank(node.get("privateFor").textValue())) {
                    privateFor = Lists.newArrayList(node.get("privateFor").textValue());
                }
            }
            
            request.setPrivateFor(privateFor);
        }

        if (null != node.get("code")) {
            request.setCode(node.get("code").textValue());
        }

        if (null != node.get("code_type")) {
            request.setCode_type(node.get("code_type").textValue());
        }
        
        if (null != node.get("from")) {
            request.setFrom(node.get("from").textValue());
        }
        
        if (null != node.get("binary")) {
            request.setBinary(node.get("binary").textValue());
        }

        if (null != node.get("privateFrom")) {
            request.setPrivateFrom(node.get("privateFrom").textValue());
        }
        
        if (null != node.get("address")) {
            request.setAddress(node.get("address").textValue());
        }
        
        if (null != node.get("method")) {
            request.setMethod(node.get("method").textValue());
        }
        
        if (null != node.get("blockNumber")) {
            request.setBlockNumber(node.get("blockNumber").textValue());
        }
        
        if (null != node.get("optimize")) {
            request.setOptimize(node.get("optimize").booleanValue());
        }

        if (null != node.get("args")) {
            JsonNode argsNode = node.get("args");
            List<Object> args = null;

            if (argsNode.isArray()) {
                args = Lists.newArrayList();

                for (Iterator<JsonNode> iter = argsNode.elements(); iter.hasNext();) {
                	JsonNode arrEntry = iter.next();

                	if (arrEntry.isArray()) {
                		List <Object> argArr = new ArrayList<>();

                		for (Iterator <JsonNode> iter2 = arrEntry.elements(); iter2.hasNext();) {
                			JsonNode arrEntry2 = iter2.next();
                			argArr.add(arrEntry2.asText());
                		}

                		args.add(argArr);
                	} else {
                		args.add(arrEntry.asText());
                	}
                }
            } else {
                if (StringUtils.isNotBlank(node.get("args").textValue())) {
                    args = Lists.newArrayList(node.get("args").textValue());
                }
            }

            request.setArgs(args.toArray());
        }

        return request;
    }
}
