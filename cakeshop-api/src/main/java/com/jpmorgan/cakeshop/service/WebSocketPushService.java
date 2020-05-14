package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;

/**
 *
 * @author Michael Kazansky
 */
public interface WebSocketPushService {

    public final String CONTRACT_TOPIC = "/topic/contract";
    public final String NODE_TOPIC = "/topic/node/status";
    public final String BLOCK_TOPIC = "/topic/block";
    public final String PENDING_TRANSACTIONS_TOPIC = "/topic/pending/transactions";
    public final String TRANSACTION_TOPIC = "/topic/transaction/";
    public final Integer MAX_ASYNC_POOL = 100;

    public void pushNodeStatus() throws APIException;

    public void pushLatestBlocks() throws APIException;

    public void pushTransactions() throws APIException;
}
