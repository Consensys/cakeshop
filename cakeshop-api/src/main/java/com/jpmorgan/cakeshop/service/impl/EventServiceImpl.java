package com.jpmorgan.cakeshop.service.impl;

import static com.jpmorgan.cakeshop.util.AbiUtils.*;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.Event;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.EventService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.util.AbiUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger LOG = LoggerFactory.getLogger(EventServiceImpl.class);

    class BlockRangeFilter {
        private final String fromBlock;
        private final String toBlock;

        public BlockRangeFilter(Long fromBlock, Long toBlock) {
            this.fromBlock = AbiUtils.toHex(fromBlock);
            this.toBlock = AbiUtils.toHex(toBlock);
        }

        public String getFromBlock() {
            return fromBlock;
        }

        public String getToBlock() {
            return toBlock;
        }
    }

    @Autowired
    private GethHttpService gethService;

    @Autowired
    private ContractService contractService;

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> listForBlock(Long blockNumber) throws APIException {
        Map<String, Object> res = gethService.executeGethCall("eth_getLogs", new Object[] { new BlockRangeFilter(blockNumber, blockNumber) });
        List<Map<String, Object>> results = (List<Map<String, Object>>) res.get("_result");
        return processEvents(results);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Event> processEvents(List<Map<String, Object>> txnEvents) throws APIException {

        List<Event> events = new ArrayList<>();
        for (Map<String, Object> data : txnEvents) {

            Event event = new Event();
            event.setId(new BigInteger(String.valueOf(System.nanoTime())));
            event.setBlockId((String) data.get("blockHash"));
            event.setBlockNumber(toBigInt("blockNumber", data));

            event.setLogIndex(toBigInt("logIndex", data));
            event.setTransactionId((String) data.get("transactionHash"));
            event.setContractId((String) data.get("address"));

            // Fetch the associated contract by it's ID so we can decode the log data (requires registry)
            Contract contract = null;
            try {
                contract = contractService.get(event.getContractId());
            } catch (APIException e) {
                if (e.getMessage().contains("eth_call failed (returned 0 bytes)")) {
                    // contract registry likely doesn't exist on this chain
                    // TODO deploy registry to external chains
                    events.add(event);
                    continue;
                }
                throw e;
            }

            if (contract == null || contract.getABI() == null) {
                // TODO can't process this event
                // this will occur when loading a transaction related to a contract deploy
                // because it isn't yet registered
                continue;
            }

            ContractABI abi = ContractABI.fromJson(contract.getABI());

            List<String> topics = (List<String>) data.get("topics");
            String eventSigHash = (topics).get(0);

            com.jpmorgan.cakeshop.model.ContractABI.Event abiEvent = abi.findEventBySignature(eventSigHash);
            event.setName(abiEvent.name);

            byte[] logData = Hex.decode(((String) data.get("data")).substring(2));
            byte[][] topicData = new byte[topics.size()][];
            for (int i = 0; i < topics.size(); i++) {
                String t = topics.get(i);
                topicData[i] = Hex.decode(t.substring(2));
            }

            Object[] decodeHex = abiEvent.decode(logData, topicData).toArray();
            event.setData(decodeHex);

            events.add(event);
        }
        return events;
    }



    @Override
    public String serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(out)) {
            os.writeObject(obj);
        }
        return out.toString("ISO-8859-1");
    }

    @Override
    public Object deserialize(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes("ISO-8859-1"));
        Object deserialized;
        try (ObjectInputStream is = new ObjectInputStream(in)) {
            deserialized = is.readObject();
        }
        return deserialized;
    }


}
