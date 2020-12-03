package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.TransactionService;
import com.jpmorgan.cakeshop.util.CakeshopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Test(enabled=false)
public class Util extends BaseGethRpcTest {

	@Autowired
	ContractService contractService;

	@Autowired
	TransactionService transactionService;

	@Autowired
	GethHttpService geth;

    public static final String ENODE_ADDRESS = "enode://abcd@1.2.3.4:1111?raftport=2222";
    public static final String ENODE_ADDRESS_NO_RAFT = "enode://abcd@1.2.3.4:1111";

    @Test
    public void testCreateEnodeURL() throws IOException {
        String enodeURL = CakeshopUtils.formatEnodeUrl("abcd", "1.2.3.4", "1111", "2222");
        assertEquals(enodeURL, ENODE_ADDRESS);
    }

    @Test
    public void testCreateEnodeURL_noRaft() throws IOException {
        String enodeURL = CakeshopUtils.formatEnodeUrl("abcd", "1.2.3.4", "1111", null);
        assertEquals(enodeURL, ENODE_ADDRESS_NO_RAFT);
    }
}
