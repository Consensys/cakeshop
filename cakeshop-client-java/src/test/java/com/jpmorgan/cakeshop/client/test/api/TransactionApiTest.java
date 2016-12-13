package com.jpmorgan.cakeshop.client.test.api;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.client.api.TransactionApi;
import com.jpmorgan.cakeshop.client.model.Transaction;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;

public class TransactionApiTest extends BaseApiTest {

    @Test
    public void testGet() {
        TransactionApi txnApi = apiClient.buildClient(TransactionApi.class);
        String hash = "0x868e24c5d213f3cef0026120b330a3884c68b941f635ab2486685f17a607b0d8";

        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"0x868e24c5d213f3cef0026120b330a3884c68b941f635ab2486685f17a607b0d8\",\"type\":\"transaction\",\"attributes\":{\"id\":\"0x868e24c5d213f3cef0026120b330a3884c68b941f635ab2486685f17a607b0d8\",\"status\":\"committed\",\"nonce\":\"0x3\",\"blockId\":\"0xb858713990d2d43cb86b9c96d14156ffab8dcd579d0b33ef00d21ec660bdc8bf\",\"blockNumber\":4,\"transactionIndex\":0,\"from\":\"0x2e219248f44546d966808cdd20cb6c36df6efa82\",\"to\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"value\":0,\"gas\":10000000,\"gasPrice\":21134605703,\"input\":\"0x60fe47b10000000000000000000000000000000000000000000000000000000000000032\",\"decodedInput\":{\"method\":\"set\",\"args\":[50]},\"cumulativeGasUsed\":26624,\"gasUsed\":26624,\"contractAddress\":null,\"logs\":null}},\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<APIData<Transaction>, Transaction> res = txnApi.get(hash);
        assertNotNull(res);
        Transaction tx = res.getData();
        assertNotNull(tx);
        assertEquals(tx.getId(), hash);
        assertEquals(tx.getBlockId(), "0xb858713990d2d43cb86b9c96d14156ffab8dcd579d0b33ef00d21ec660bdc8bf");
    }

}
