package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.DirectTransactionRequest;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface TransactionService {

    /**
     * Get transaction information for the given ID
     *
     * @param id
     * @return
     * @throws APIException
     */
	public Transaction get(String id) throws APIException;

	/**
	 * Get a batch of transactions in a single request.
	 *
	 * @param ids
	 * @return
	 * @throws APIException
	 */
	public List<Transaction> get(List<String> ids) throws APIException;

	/**
	 * List transactions in the given block
	 *
	 * @param blockHash
	 * @param blockNumber
	 * @return
	 * @throws APIException
	 */
	public List<Transaction> list(String blockHash, Integer blockNumber) throws APIException;

	/**
	 * Fetch transactions in a pending state (not yet committed)
	 *
	 * @return
	 * @throws APIException
	 */
	public List<Transaction> pending() throws APIException;

	/**
	 * Wait for the given Transaction to be mined (blocks until completed)
	 *
	 * @param result
     * @param pollDelay
     * @param pollDelayUnit
	 * @return {@link Transaction}
	 * @throws APIException
	 * @throws InterruptedException
	 */
    public Transaction waitForTx(TransactionResult result, long pollDelay, TimeUnit pollDelayUnit) throws APIException, InterruptedException;


    /**
	 * @param request
	 * @return {@link TransactionResult}
     * @throws com.jpmorgan.cakeshop.error.APIException
	 */
    public TransactionResult directTransact(DirectTransactionRequest request) throws APIException;

    /**
     * Load the private payload, if the Transaction is a private one.
     *
     * NOTE: Replaces tx.input with the private version. If you want to preserve the original, hash, you must do so yourself.
     *
     * @param tx
     */
    void loadPrivatePayload(Transaction tx);

}
