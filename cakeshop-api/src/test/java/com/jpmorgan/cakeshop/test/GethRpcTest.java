package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Web3DefaultResponseType;
import org.testng.annotations.Test;
import org.web3j.protocol.core.BatchRequest;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class GethRpcTest extends BaseGethRpcTest {

    private final String expectedHash = "0xb89f8664868b71eee61daf6eb8f709e3800b77501172430cb3167cd6692a878e";

    @Test
    public void testExecWithParams() throws APIException {
        String method = "eth_getBlockByNumber";

        Map<String, Object> data = geth.executeGethCall(method, new Object[]{"latest", false});
        assertNotNull(data.get("hash"));

        data = geth.executeGethCall(method, new Object[]{"0x" + Long.toHexString(0), false});
        assertEquals(data.get("hash"), expectedHash);

        data = geth.executeGethCall("eth_getBlockByHash", new Object[]{expectedHash, false});
        assertEquals(data.get("hash"), expectedHash);
    }

    @Test
    public void testBatchExec() throws APIException {

    	BatchRequest batch = geth.getQuorumService().newBatch();
        batch.add(geth.createHttpRequestType("eth_getBlockByNumber", EthBlock.class, new Object[]{"0x" + Long.toHexString(0), false}));
        batch.add(geth.createHttpRequestType("eth_getBlockByNumber", EthBlock.class, new Object[]{"0x" + Long.toHexString(0), false}));

        List<? extends Response<?>> res;  
    	try {
    		res = batch.send().getResponses();
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}

        assertNotNull(res);
        assertEquals(res.size(), 2);

        for (Response<?> data : res) {
        	Block b = (Block)data.getResult();
            assertEquals(b.getHash(), expectedHash);
        }

    }

}
