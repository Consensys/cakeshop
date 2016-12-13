package com.jpmorgan.cakeshop.service.impl;

import static com.jpmorgan.cakeshop.util.AbiUtils.*;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.DirectTransactionRequest;
import com.jpmorgan.cakeshop.model.Event;
import com.jpmorgan.cakeshop.model.RequestModel;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.Transaction.Status;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.EventService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.WalletService;
import com.jpmorgan.cakeshop.util.StringUtils;

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
    private GethHttpService geth;

    @Autowired
    private EventService eventService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private ApplicationContext applicationContext;

    private String defaultFromAddress;

    @Override
    public Transaction get(String id) throws APIException {
        List<RequestModel> reqs = new ArrayList<>();
        reqs.add(new RequestModel("eth_getTransactionByHash", new Object[]{id}, 1L));
        reqs.add(new RequestModel("eth_getTransactionReceipt", new Object[]{id}, 2L));
        List<Map<String, Object>> batchRes = geth.batchExecuteGethCall(reqs);

        if (batchRes.isEmpty() || batchRes.get(0) == null) {
            return null;
        }

        Map<String, Object> txData = batchRes.get(0);
        if (batchRes.get(1) != null) {
            txData.putAll(batchRes.get(1));
        }

        Transaction tx = processTx(txData);

        return tx;
    }

    private Transaction processTx(Map<String, Object> txData) throws APIException {
        Transaction tx = new Transaction();
        tx.setId((String) txData.get("hash"));
        tx.setBlockId((String) txData.get("blockHash"));
        //TODO: this is a hack to make test happy. Need to evaluate the logic to have to and contract address always present.
        tx.setContractAddress((String) txData.get("contractAddress"));
        tx.setTo((String)txData.get("to"));
        //hack end
        tx.setNonce((String) txData.get("nonce"));
        tx.setInput((String) txData.get("input"));
        tx.setFrom((String) txData.get("from"));

        // add signature
        if (txData.get("r") != null) {
            tx.setR((String) txData.get("r"));
            tx.setS((String) txData.get("s"));
            tx.setV((String) txData.get("v"));
        }

        tx.setGasPrice(toBigInt("gasPrice", txData));

        tx.setTransactionIndex(toBigInt("transactionIndex", txData));
        tx.setBlockNumber(toBigInt("blockNumber", txData));
        tx.setValue(toBigInt("blockNumber", txData));
        tx.setGas(toBigInt("gas", txData));
        tx.setCumulativeGasUsed(toBigInt("cumulativeGasUsed", txData));
        tx.setGasUsed(toBigInt("gasUsed", txData));

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

        if (txData.get("logs") != null) {
            List<Map<String, Object>> logs = (List<Map<String, Object>>) txData.get("logs");
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
            Map<String, Object> res = geth.executeGethCall("eth_getQuorumPayload", new Object[] { tx.getInput().substring(2) });
            if (res.get("_result") != null) {
                tx.setInput((String) res.get("_result")); // replace input with private payload
            }
        } catch (APIException e) {
            LOG.warn("Failed to load private payload: " + e.getMessage());
        }
    }

    @Override
    public List<Transaction> get(List<String> ids) throws APIException {

        List<RequestModel> reqs = new ArrayList<>();
        for (String id : ids) {
            reqs.add(new RequestModel("eth_getTransactionByHash", new Object[]{id}, 1L));
            reqs.add(new RequestModel("eth_getTransactionReceipt", new Object[]{id}, 2L));
        }
        List<Map<String, Object>> batchRes = geth.batchExecuteGethCall(reqs);

        // merge pairs of requests for all txns into single map
        Map<String, Map<String, Object>> txnResponses = new HashMap<>();
        for (Map<String, Object> res : batchRes) {
            if (res != null) {
                String hash = null;
                if (res.get("hash") != null) {
                    hash = (String) res.get("hash");
                } else if (res.get("transactionHash") != null) {
                    hash = (String) res.get("transactionHash");
                }
                if (hash != null) {
                    Map<String, Object> map = txnResponses.get(hash);
                    if (map != null) {
                        map.putAll(res); // add to existing map
                    } else {
                        txnResponses.put(hash, res); // insert new map
                    }
                }
            }
        }

        // collect txns in the order they were requested
        List<Transaction> txns = new ArrayList<>();
        for (String id : ids) {
            Map<String, Object> txData = txnResponses.get(id);
            txns.add(processTx(txData));
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
        Map<String, Object> readRes = geth.executeGethCall("eth_sendTransaction", request.toGethArgs());
        return new TransactionResult((String) readRes.get("_result"));
    }

}
