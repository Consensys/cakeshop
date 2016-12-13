package com.jpmorgan.cakeshop.test;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.service.ContractRegistryService;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

public class ContractRegistryServiceTest extends BaseGethRpcTest {

    private static final Logger LOG = LoggerFactory.getLogger(ContractRegistryServiceTest.class);

    @Autowired
    ContractRegistryService contractRegistry;

    @Autowired
    private ContractService contractService;

	@Autowired
	private TransactionService transactionService;

	@Test
	public void testRegisterAndGet() throws IOException, InterruptedException {

	    String addr = createContract();
	    String abi = readTestFile("contracts/simplestorage.abi.txt");
	    String code = readTestFile("contracts/simplestorage.sol");

	    Long createdDate = (System.currentTimeMillis() / 1000);

	    registerContract(addr, abi, code, createdDate);

	    Contract contract = contractRegistry.getById(addr);
	    assertNotNull(contract);
	    assertEquals(contract.getAddress(), addr);
	    assertEquals(contract.getABI(), abi);
	    assertEquals(contract.getCode(), code);
	    assertEquals(contract.getCodeType(), CodeType.solidity);
	    assertNotNull(contract.getCreatedDate());
	    assertTrue(contract.getCreatedDate() >= createdDate);
	    assertEquals(contract.getName(), "SimpleStorage");

	    BigInteger val = (BigInteger) contractService.read(addr, null, "get", null, null)[0];
	    assertEquals(val.intValue(), 100);
	}

	@Test
	public void testGetInvalidId() throws APIException  {
	    Contract contract = contractRegistry.getById("0x62061a15259c8dd9c49312ddc9335333c4212abe");
	    assertNull(contract);
	}

    private void registerContract(String addr, String abi, String code, Long createdDate)
            throws APIException, InterruptedException {
        TransactionResult tr = contractRegistry.register(null, addr, "SimpleStorage", abi, code, CodeType.solidity, createdDate);
	    assertNotNull(tr);
	    assertNotNull(tr.getId());
	    assertTrue(!tr.getId().isEmpty());

	    Transaction tx = transactionService.waitForTx(tr, 50, TimeUnit.MILLISECONDS);
	    assertNotNull(tx);
    }

	@Test
	public void testList() throws IOException, InterruptedException {

	    Long createdDate = (System.currentTimeMillis() / 1000);

	    String addr = createContract();
	    String abi = readTestFile("contracts/simplestorage.abi.txt");
	    String code = readTestFile("contracts/simplestorage.sol");

	    registerContract(addr, abi, code, createdDate);
	    registerContract(addr, abi, code, createdDate);

	    List<Contract> list = contractRegistry.list();

	    assertNotNull(list);
	    assertTrue(!list.isEmpty());
	    assertTrue(list.size() > 0);

	    for (Contract c : list) {
	        if (c.getAddress().equalsIgnoreCase(addr)) {
                assertEquals(c.getAddress(), addr);
                assertEquals(c.getABI(), abi);
                assertEquals(c.getCode(), code);
                assertTrue(c.getCreatedDate() >= createdDate);
	        }
        }

	}

}
