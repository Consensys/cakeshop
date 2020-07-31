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

	/**
	 * It turns out that the compiled binary code (using solc) differs from the
	 * binary code as deployed to the chain and retrieved using eth_getCode. This
	 * helper method assists in retrieving the deployed version for use in, e.g.,
	 * the genesis block file.
	 *
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test(enabled=false)
	public void testGetDeployedBinaryForContract() throws IOException, InterruptedException {

	    String code = readTestFile("contracts/ContractRegistry.sol");

		String contractAddress = createContract(code, null, "ContractRegistry.sol");

		Contract contract = contractService.get(contractAddress);
		assertNotNull(contract);
		assertNotNull(contract.getBinary(), "Binary code should be present");

		System.out.println("BINARY CODE:");
		System.out.println(contract.getBinary());
	}

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
