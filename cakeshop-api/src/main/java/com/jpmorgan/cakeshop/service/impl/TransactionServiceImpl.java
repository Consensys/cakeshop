package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.DirectTransactionRequest;
import com.jpmorgan.cakeshop.model.Event;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.Transaction.Status;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.EventService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.WalletService;
import com.jpmorgan.cakeshop.util.StringUtils;

import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private EventService eventService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private ApplicationContext applicationContext;

    private String defaultFromAddress;

    @Override
    public Transaction get(String id) throws APIException {
        BatchRequest batch = gethService.getQuorumService().newBatch();
        batch.add(gethService.createHttpRequestType("eth_getTransactionByHash", EthTransaction.class, new Object[]{id}));
        batch.add(gethService.createHttpRequestType("eth_getTransactionReceipt", EthGetTransactionReceipt.class, new Object[]{id}));

        List<? extends Response<?>> res; 
    	try {
    		res = batch.send().getResponses();
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	
        if (res == null || res.isEmpty() || res.get(0) == null) {
            return null;
        }

        org.web3j.protocol.core.methods.response.Transaction txData = ((EthTransaction) res.get(0)).getTransaction().get();
        TransactionReceipt tr = null;
        
        if (res.get(1) != null) {
        	tr = ((EthGetTransactionReceipt)res.get(1)).getTransactionReceipt().get();
        }

        Transaction tx = processTx(txData, tr);

        return tx;
    }

    private Transaction processTx(org.web3j.protocol.core.methods.response.Transaction t, TransactionReceipt tr) throws APIException {
        Transaction tx = new Transaction();
        tx.setId(t.getHash());
        tx.setBlockId(t.getBlockHash());
        tx.setTo(t.getTo());
        tx.setNonce(t.getNonceRaw());
        tx.setInput(t.getInput());
        tx.setFrom(t.getFrom());

        // add signature
        if (t.getR() != null) {
            tx.setR(t.getR());
            tx.setS(t.getS());
            tx.setV(t.getS());
        }
        
        if (tr != null) {
            //TODO: this is a hack to make test happy. Need to evaluate the logic to have to and contract address always present.
            tx.setContractAddress(tr.getContractAddress());
            //hack end
            tx.setCumulativeGasUsed(tr.getCumulativeGasUsed());
            tx.setGasUsed(tr.getGasUsed());
            tx.setReturnCode(tr.getStatus());
        }

        tx.setGasPrice(t.getGasPrice());
        tx.setTransactionIndex(t.getTransactionIndex());
        tx.setBlockNumber(t.getBlockNumber());
        tx.setValue(t.getValue());
        tx.setGas(t.getGas());

        if (tx.getBlockId() == null || tx.getBlockNumber() == null
                || tx.getBlockId().contentEquals("0x0000000000000000000000000000000000000000000000000000000000000000")) {

            tx.setStatus(Status.pending);
        } else {
            tx.setStatus(Status.committed);
        }

        if (tx.getContractAddress() == null && tx.getStatus() == Status.committed) {

            // lookup contract
            ContractService contractService = applicationContext.getBean(ContractService.class);
            Contract contract = null;
            try {
                contract = contractService.get(tx.getTo());
            } catch (APIException e) {}

            String origInput = tx.getInput();
            if (contract != null && contract.getABI() != null && !contract.getABI().isEmpty()) {
                ContractABI abi = ContractABI.fromJson(contract.getABI());
                loadPrivatePayload(tx);
                tx.decodeContractInput(abi);
                tx.setInput(origInput); // restore original input after [gemini] decode

            } else if (contract == null) {
                // if contract doesn't exist, assume it was a 'direct' txn, e.g., a raw payload
                // TODO add a better check
                loadPrivatePayload(tx);
                tx.decodeDirectTxnInput(tx.getInput());
                tx.setInput(origInput); // restore original input after [gemini] decode
            }
        }

        if (tr != null && tr.getLogs() != null) {
            List<Log> logs = (List<Log>) tr.getLogs();
            if (!logs.isEmpty()) {
                List<Event> events = eventService.processEvents(logs);
                tx.setLogs(events);
            }
        }

        return tx;
    }

    /**
     * Load private payloads
     *
     * @param tx
     */
    @Override
    public void loadPrivatePayload(Transaction tx) {
        if (tx.isPublic()) {
            return;
        }

        try {
            // TODO use txn manager
        	String pp = gethService.getQuorumService().quorumGetPrivatePayload(tx.getInput().substring(2)).send().getPrivatePayload();
            if (pp != null) {
                tx.setInput(pp); // replace input with private payload
            }
        } catch (Exception e) {
            LOG.warn("Failed to load private payload: " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> get(List<String> ids) throws APIException {
        BatchRequest batch = gethService.getQuorumService().newBatch();
        for (String id : ids) {
        	batch.add(gethService.createHttpRequestType("eth_getTransactionByHash", EthTransaction.class, new Object[]{id}));
        	batch.add(gethService.createHttpRequestType("eth_getTransactionReceipt", EthGetTransactionReceipt.class, new Object[]{id}));
        }

        List<? extends Response<?>> res; 
    	try {
    		res = batch.send().getResponses();
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	
        //collect all txs and txreceipts by txHash
        Map<String, org.web3j.protocol.core.methods.response.Transaction> txMap = new HashMap<>();
        Map<String, TransactionReceipt> trMap = new HashMap<>();
        
        for (Response<?> r : res) {
        	if (r instanceof EthTransaction) {
        		org.web3j.protocol.core.methods.response.Transaction t = ((EthTransaction) r).getTransaction().get();
        		txMap.put(t.getHash(), t);
        	} else if (r instanceof EthGetTransactionReceipt) {
        		TransactionReceipt receipt = ((EthGetTransactionReceipt) r).getResult();
        		trMap.put(receipt.getTransactionHash(), receipt);
        	}
        }

        // collect txns in the order they were requested
        List<Transaction> txns = new ArrayList<>();
        for (String id : ids) {
            txns.add(processTx(txMap.get(id), trMap.get(id)));
        }

        return txns;
    }

    @Override
    public List<Transaction> list(String blockHash, Integer blockNumber) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Transaction> pending() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Transaction waitForTx(TransactionResult result, long pollDelay, TimeUnit pollDelayUnit)
            throws APIException, InterruptedException {

        Transaction tx = null;
        while (true) {
            tx = this.get(result.getId());
            if (tx.getStatus() == null ? Status.committed.toString() == null : tx.getStatus().equals(Status.committed)) {
                break;
            }
            pollDelayUnit.sleep(pollDelay);
        }
        return tx;
    }

    @Override
    public TransactionResult directTransact(DirectTransactionRequest request) throws APIException {
        if (defaultFromAddress == null) {
            defaultFromAddress = walletService.list().get(0).getAddress();
        }
        request.setFromAddress(
                StringUtils.isNotBlank(request.getFromAddress())
                        ? request.getFromAddress()
                        : defaultFromAddress); // make sure we have a non-null from address
        String hash = null; 
        try {
        	hash = gethService.getQuorumService().ethSendTransaction(request.toPrivateTransaction()).send().getTransactionHash();
        } catch (IOException e) {
        	throw new APIException(e.getMessage());
        }
        if (hash == null) {
        	throw new APIException("transaction failure");
        }
        return new TransactionResult(hash);
    }

}
