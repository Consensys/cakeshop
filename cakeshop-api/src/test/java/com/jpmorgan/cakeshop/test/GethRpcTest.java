package com.jpmorgan.cakeshop.test;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.RequestModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

public class GethRpcTest extends BaseGethRpcTest {

    private final String expectedHash = "0xd93b8da4c2f48c98e2cb76bef403ec22cada28331946218487b0fd1335e52bdd";

    @Test
    public void testExecWithParams() throws APIException {
        String method = "eth_getBlockByNumber";

        Map<String, Object> data = geth.executeGethCall(method, new Object[]{ "latest", false });
        assertNotNull(data.get("hash"));

        data = geth.executeGethCall(method, new Object[]{ 0, false });
        assertEquals(data.get("hash"), expectedHash);

        data = geth.executeGethCall("eth_getBlockByHash", new Object[]{ expectedHash, false });
        assertEquals(data.get("hash"), expectedHash);
    }

    @Test
    public void testBatchExec() throws APIException {

        List<RequestModel> reqs = new ArrayList<>();
        reqs.add(new RequestModel("eth_getBlockByNumber", new Object[]{ 0, false }, 1L));
        reqs.add(new RequestModel("eth_getBlockByNumber", new Object[]{ 0, false }, 1L));

        List<Map<String, Object>> batchRes = geth.batchExecuteGethCall(reqs);

        assertNotNull(batchRes);
        assertEquals(batchRes.size(), 2);

        for (Map<String, Object> data : batchRes) {
            assertEquals(data.get("hash"), expectedHash);
        }

    }

}
