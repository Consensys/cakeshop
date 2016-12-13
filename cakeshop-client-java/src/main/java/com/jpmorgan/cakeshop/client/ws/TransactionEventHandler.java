package com.jpmorgan.cakeshop.client.ws;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.reflect.TypeToken;
import com.jpmorgan.cakeshop.client.model.Transaction;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

public abstract class TransactionEventHandler extends EventHandler<Transaction> {

    @SuppressWarnings("serial")
    private static final TypeToken<APIResponse<APIData<Transaction>, Transaction>> typeToken =
            new TypeToken<APIResponse<APIData<Transaction>, Transaction>>(){};

    private static final JavaType valType = objectMapper.constructType(typeToken.getType());

    public static final String TOPIC = "/topic/transaction/";
    public static final String TOPIC_ALL = "/topic/transaction/all";

    private final String txId;

    public TransactionEventHandler() {
        this(null);
    }

    public TransactionEventHandler(String txId) {
        this.txId = txId;
    }

    @Override
    public String getTopic() {
        if (txId != null) {
            return TOPIC + txId;
        } else {
            return TOPIC_ALL;
        }
    }

    @Override
    public JavaType getValType() {
        return valType;
    }

}
