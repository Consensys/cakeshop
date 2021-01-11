package com.jpmorgan.cakeshop.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.*;
import com.jpmorgan.cakeshop.repo.BlockRepository;
import com.jpmorgan.cakeshop.repo.ContractRepository;
import com.jpmorgan.cakeshop.repo.EventRepository;
import com.jpmorgan.cakeshop.repo.TransactionRepository;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.WebSocketPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@Scope("prototype")
@Profile("!test")
public class SavingBlockListener implements BlockListener {

    private class BlockSaverThread extends Thread {
        public boolean running = true;

        public BlockSaverThread() {
            setName("BlockSaver-" + getId());
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Block block = blockQueue.poll(500, TimeUnit.MILLISECONDS);
                    if (block != null) {
                        saveBlock(block);
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (Throwable ex) {
                    LOG.error("BlockSaverThread died", ex);
                }
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SavingBlockListener.class);

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private TransactionService txService;

    @Autowired
    private ObjectMapper jsonMapper;

    private final ArrayBlockingQueue<Block> blockQueue;

    private final BlockSaverThread blockSaver;

    private static final String TOPIC = WebSocketPushService.TRANSACTION_TOPIC + "all";
    private static final String TOPIC_BLOCK = WebSocketPushService.BLOCK_TOPIC + "/all";


    @Autowired(required = false)
    private SimpMessagingTemplate stompTemplate;

    public SavingBlockListener() {
        blockQueue = new ArrayBlockingQueue<>(1000);
        blockSaver = new BlockSaverThread();
    }

    @PostConstruct
    protected void init() {
        LOG.debug("starting " + blockSaver.getId());
        blockSaver.start();
    }

    @PreDestroy
    @Override
    public void shutdown() {
        LOG.info("Stopping SavingBlockListener (thread id=" + blockSaver.getId() + ")");
        blockSaver.running = false;
    }

    protected void saveBlock(Block block) {
        LOG.info("Persisting block #" + block.getNumber());
        blockRepository.save(block);
        Lists.partition(block.getTransactions(), 256).stream()
            .flatMap(txnChunk -> {
                try {
                    return txService.get(txnChunk).stream();
                } catch (APIException e) {
                    LOG.warn("Failed to load transaction details for tx", e);
                    return Stream.empty();
                }
            })
            .forEach(transaction -> {
                // because everything has IDs already, cascading doesn't seem to work. Save child events first
                if (transaction.getLogs() != null) {
                    transaction.getLogs().forEach(event -> LOG.info("Saving event {}", event));
                    eventRepository.saveAll(transaction.getLogs());
                }
                transactionRepository.save(transaction);
                if (transaction.getContractAddress() != null) {
                    long createdDate = block.getTimestamp().longValue();
                    LOG.info("Transaction is a contract creation transaction, adding to database with empty details, {}", transaction.getContractAddress());
                    Contract contract = new Contract(transaction.getContractAddress(), null, null, null, null, null, createdDate, null, null);
                    String contractJson = null;
                    try {
                        contractJson = jsonMapper.writeValueAsString(contract);
                        ContractInfo contractInfo = new ContractInfo(
                            transaction.getContractAddress(),
                            null,
                            transaction.getFrom(),
                            createdDate,
                            contractJson);
                        contractRepository.save(contractInfo);
                    } catch (JsonProcessingException e) {
                        LOG.error("Error saving contract {}", transaction.getContractAddress(), e);
                    }
                }
            });
        pushBlockNumber(block.getNumber().longValue()); // push to subscribers after saving
    }

    private void pushBlockNumber(Long blockNumber) {
        if (stompTemplate == null) {
            return;
        }
        LOG.debug("Pushing block number " + blockNumber);
        APIResponse res = new APIResponse();
        res.setData(new APIData(blockNumber.toString(), "block_number", blockNumber));
        stompTemplate.convertAndSend(TOPIC_BLOCK, res);
    }

    private void pushTransactions(List<Transaction> txns) {
        if (stompTemplate == null) {
            return;
        }
        LOG.debug("Transaction list size " + txns.size());
        for (Transaction txn : txns) {

            APIResponse res = new APIResponse();
            res.setData(txn.toAPIData());
            stompTemplate.convertAndSend(TOPIC, res);
        }
    }

    @Override
    public void blockCreated(Block block) {
        try {
            blockQueue.put(block);
        } catch (InterruptedException e) {
            return;
        }
    }

}
