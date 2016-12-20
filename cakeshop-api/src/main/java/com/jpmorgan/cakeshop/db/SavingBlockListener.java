package com.jpmorgan.cakeshop.db;

import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.jpmorgan.cakeshop.dao.BlockDAO;
import com.jpmorgan.cakeshop.dao.TransactionDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Block;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.WebSocketPushService;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
    private BlockDAO blockDAO;

    @Autowired
    private TransactionDAO txDAO;

    @Autowired
    private TransactionService txService;

    @Autowired
    private GethConfigBean gethConfig;

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
        if (!gethConfig.isDbEnabled()) {
            return;
        }

        LOG.debug("Persisting block #" + block.getNumber());
        blockDAO.save(block);
        if (!block.getTransactions().isEmpty()) {
            List<String> transactions = block.getTransactions();
            List<List<String>> txnChunks = Lists.partition(transactions, 256);
            for (List<String> txnChunk : txnChunks) {
                try {
                    List<Transaction> txns = txService.get(txnChunk);
                    txDAO.save(txns);
                } catch (APIException e) {
                    LOG.warn("Failed to load transaction details for tx", e);
                }
            }
            pushBlockNumber(block.getNumber().longValue()); // push to subscribers after saving
        }
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
        if (!gethConfig.isDbEnabled()) {
            return;
        }
        try {
            blockQueue.put(block);
        } catch (InterruptedException e) {
            return;
        }
    }

}
