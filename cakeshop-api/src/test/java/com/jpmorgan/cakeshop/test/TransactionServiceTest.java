package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.Transaction.Status;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class TransactionServiceTest extends BaseGethRpcTest {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceTest.class);

    @Autowired
    private ContractService contractService;

    @Autowired
    private TransactionService transactionService;

    @Test
    public void testGet() throws IOException {
        String code = readTestFile("contracts/simplestorage.sol");

        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, new Object[] { 100 }, null, null, null,
            "simplestorage.sol", true, "constantinople", null);
        LOG.info("EXECUTING testGet ");
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(!result.getId().isEmpty());

        Transaction tx = transactionService.get(result.getId());
        assertNotNull(tx);
        assertNotNull(tx.getId());
        assertEquals(tx.getId(), result.getId());
        assertEquals(tx.getStatus(), Status.committed);
    }

    @Test
    public void testGetBatch() throws IOException, InterruptedException {

        String code = readTestFile("contracts/simplestorage.sol");
        LOG.info("EXECUTING testGetBatch 1 ");
        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, new Object[] { 100 }, null, null, null,
            "simplestorage.sol", true, "constantinople", null);
        LOG.info("EXECUTING testGetBatch 2 ");
        TransactionResult result2 = contractService.create(null, code, ContractService.CodeType.solidity, new Object[] { 100 }, null, null, null,
            "simplestorage.sol", true, "constantinople", null);

        List<Transaction> txns = transactionService.get(Lists.newArrayList(result.getId(), result2.getId()));
        assertNotNull(txns);
        assertEquals(txns.size(), 2);
        assertEquals(txns.get(0).getId(), result.getId());
        assertEquals(txns.get(1).getId(), result2.getId());
    }

    @Test
    public void testGetPendingTx() throws IOException, InterruptedException {
        String code = readTestFile("contracts/simplestorage.sol");
        ContractABI abi = ContractABI.fromJson(readTestFile("contracts/simplestorage.abi.txt"));

        LOG.info("EXECUTING testGetPendingTx ");
        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, new Object[] { 100 }, null, null, null,
            "simplestorage.sol", true, "constantinople", null);
        assertNotNull(result);
        assertNotNull(result.getId());

        LOG.info("EXECUTING testGetPendingTx 2");
        Transaction createTx = transactionService.waitForTx(result, 20, TimeUnit.MILLISECONDS);
    }
}
