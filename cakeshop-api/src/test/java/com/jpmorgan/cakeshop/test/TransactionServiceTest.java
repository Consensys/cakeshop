package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.bean.GethConfigBean;
import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.Transaction.Status;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.TransactionService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert.ThrowingRunnable;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

public class TransactionServiceTest extends BaseGethRpcTest {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceTest.class);

    @Autowired
    private ContractService contractService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private GethConfigBean gethConfig;

    @Test
    public void testGet() throws IOException {
        String code = readTestFile("contracts/simplestorage.sol");

        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, null, null, null, null);
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
        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, null, null, null, null);
        LOG.info("EXECUTING testGetBatch 2 ");
        TransactionResult result2 = contractService.create(null, code, ContractService.CodeType.solidity, null, null, null, null);

        List<Transaction> txns = transactionService.get(Lists.newArrayList(result.getId(), result2.getId()));
        assertNotNull(txns);
        assertEquals(txns.size(), 2);
        assertEquals(txns.get(0).getId(), result.getId());
        assertEquals(txns.get(1).getId(), result2.getId());
    }

    @Test
    public void testGetInvalidHash() throws IOException {
        assertThrows(APIException.class, new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                transactionService.get("0xasdf");
            }
        });

        assertNull(transactionService.get("0x19ceb68f8df155fb05029ef89dc7e74d0f7bd7c97f9837cc659381ecb9e8eb49"));
    }

    @Test
    public void testGetPendingTx() throws IOException, InterruptedException {
        String code = readTestFile("contracts/simplestorage.sol");
        ContractABI abi = ContractABI.fromJson(readTestFile("contracts/simplestorage.abi.txt"));

        LOG.info("EXECUTING testGetPendingTx ");
        TransactionResult result = contractService.create(null, code, ContractService.CodeType.solidity, null, null, null, null);
        assertNotNull(result);
        assertNotNull(result.getId());

        LOG.info("EXECUTING testGetPendingTx 2");
        Transaction createTx = transactionService.waitForTx(result, 20, TimeUnit.MILLISECONDS);

        // stop mining and submit tx
        if (!gethConfig.isQuorum()) {
            Map<String, Object> res = geth.executeGethCall("miner_stop", new Object[]{});
        }
        TransactionResult tr = contractService.transact(createTx.getContractAddress(), abi, null, "set", new Object[]{200});

        Transaction tx = transactionService.get(tr.getId());
        assertNotNull(tx);
        assertEquals(tx.getId(), tr.getId());
        assertEquals(tx.getStatus(), Status.pending);
    }

}
