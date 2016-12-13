package com.jpmorgan.cakeshop.client.test.api;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.client.api.NodeApi;
import com.jpmorgan.cakeshop.client.model.Node;
import com.jpmorgan.cakeshop.client.model.req.NodeUpdateCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;
import com.jpmorgan.cakeshop.client.model.res.SimpleResult;

import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;

public class NodeApiTest extends BaseApiTest {

    @Test
    public void testGet() {
        NodeApi nodeApi = apiClient.buildClient(NodeApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"40f33eabe7cb50e184082e749d296a27746fb53416a3e9a3cd24167c8d8170ad9e29e575256a8a6f645d9bba80c665e10d89e7f8d12dca5638449fa1f092cc3b\",\"type\":\"node\",\"attributes\":{\"status\":\"running\",\"id\":\"40f33eabe7cb50e184082e749d296a27746fb53416a3e9a3cd24167c8d8170ad9e29e575256a8a6f645d9bba80c665e10d89e7f8d12dca5638449fa1f092cc3b\",\"peerCount\":0,\"latestBlock\":4,\"pendingTxn\":0,\"mining\":true,\"nodeUrl\":\"enode://40f33eabe7cb50e184082e749d296a27746fb53416a3e9a3cd24167c8d8170ad9e29e575256a8a6f645d9bba80c665e10d89e7f8d12dca5638449fa1f092cc3b@192.168.80.227:30303?discport=0\",\"nodeName\":\"Geth/v1.4.3-stable-6bdcafda/darwin/go1.5.2/chetan\",\"nodeIP\":\"192.168.80.227\",\"rpcUrl\":\"http://localhost:8102\",\"dataDirectory\":\"/Users/chetan/Documents/workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/data/enterprise-ethereum/local/ethereum\",\"config\":{\"identity\":\"chetan\",\"committingTransactions\":true,\"networkId\":1006,\"logLevel\":3,\"genesisBlock\":\"{\\n    \\\"nonce\\\": \\\"0xdeadbeefdeadbeef\\\",\\n    \\\"timestamp\\\": \\\"0x00\\\",\\n    \\\"parentHash\\\": \\\"0x0000000000000000000000000000000000000000000000000000000000000000\\\",\\n    \\\"extraData\\\": \\\"0x686f727365\\\",\\n    \\\"gasLimit\\\": \\\"0x08000000\\\",\\n    \\\"difficulty\\\": \\\"0x0400\\\",\\n    \\\"mixhash\\\": \\\"0x0000000000000000000000000000000000000000000000000000000000000000\\\",\\n    \\\"coinbase\\\": \\\"0x3333333333333333333333333333333333333333\\\",\\n    \\\"alloc\\\": {\\n      \\\"0x2e219248f44546d966808cdd20cb6c36df6efa82\\\": {\\n        \\\"Balance\\\": \\\"1606938044258990275541962092341162602522202993782792835301376\\\"\\n      },\\n      \\\"0xcd5b17da5ad176905c12fc85ce43ec287ab55363\\\": {\\n        \\\"Balance\\\": \\\"1606938044258990275541962092341162602522202993782792835301376\\\"\\n      },\\n      \\\"0x50bb02281de5f00cc1f1dd5a6692da3fa9b2d912\\\": {\\n        \\\"Balance\\\": \\\"1606938044258990275541962092341162602522202993782792835301376\\\"\\n      }\\n   }\\n}\\n\",\"extraParams\":\"\"},\"peers\":[]}},\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<APIData<Node>, Node> res = nodeApi.get();

        assertNotNull(res);
        Node node = res.getData();
        assertNotNull(node);
        assertEquals(node.getPeers().size(), 0);
        assertEquals(node.getPeerCount(), Integer.valueOf(0));
    }

    @Test
    public void testUpdate() {
        NodeApi nodeApi = apiClient.buildClient(NodeApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"40f33eabe7cb50e184082e749d296a27746fb53416a3e9a3cd24167c8d8170ad9e29e575256a8a6f645d9bba80c665e10d89e7f8d12dca5638449fa1f092cc3b\",\"type\":\"node\",\"attributes\":{\"status\":\"running\",\"id\":\"40f33eabe7cb50e184082e749d296a27746fb53416a3e9a3cd24167c8d8170ad9e29e575256a8a6f645d9bba80c665e10d89e7f8d12dca5638449fa1f092cc3b\",\"peerCount\":0,\"latestBlock\":4,\"pendingTxn\":0,\"mining\":false,\"nodeUrl\":\"enode://40f33eabe7cb50e184082e749d296a27746fb53416a3e9a3cd24167c8d8170ad9e29e575256a8a6f645d9bba80c665e10d89e7f8d12dca5638449fa1f092cc3b@192.168.80.227:30303?discport=0\",\"nodeName\":\"Geth/v1.4.3-stable-6bdcafda/darwin/go1.5.2/chetan\",\"nodeIP\":\"192.168.80.227\",\"rpcUrl\":\"http://localhost:8102\",\"dataDirectory\":\"/Users/chetan/Documents/workspace/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/data/enterprise-ethereum/local/ethereum\",\"config\":{\"identity\":\"chetan\",\"committingTransactions\":false,\"networkId\":1006,\"logLevel\":3,\"genesisBlock\":\"{\\n    \\\"nonce\\\": \\\"0xdeadbeefdeadbeef\\\",\\n    \\\"timestamp\\\": \\\"0x00\\\",\\n    \\\"parentHash\\\": \\\"0x0000000000000000000000000000000000000000000000000000000000000000\\\",\\n    \\\"extraData\\\": \\\"0x686f727365\\\",\\n    \\\"gasLimit\\\": \\\"0x08000000\\\",\\n    \\\"difficulty\\\": \\\"0x0400\\\",\\n    \\\"mixhash\\\": \\\"0x0000000000000000000000000000000000000000000000000000000000000000\\\",\\n    \\\"coinbase\\\": \\\"0x3333333333333333333333333333333333333333\\\",\\n    \\\"alloc\\\": {\\n      \\\"0x2e219248f44546d966808cdd20cb6c36df6efa82\\\": {\\n        \\\"Balance\\\": \\\"1606938044258990275541962092341162602522202993782792835301376\\\"\\n      },\\n      \\\"0xcd5b17da5ad176905c12fc85ce43ec287ab55363\\\": {\\n        \\\"Balance\\\": \\\"1606938044258990275541962092341162602522202993782792835301376\\\"\\n      },\\n      \\\"0x50bb02281de5f00cc1f1dd5a6692da3fa9b2d912\\\": {\\n        \\\"Balance\\\": \\\"1606938044258990275541962092341162602522202993782792835301376\\\"\\n      }\\n   }\\n}\\n\",\"extraParams\":\"\"},\"peers\":[]}},\"meta\":{\"version\":\"1.0\"}}"));
        Node node = nodeApi.update(new NodeUpdateCommand().commitingTransactions(false)).getData();
        assertNotNull(node);
        assertTrue(node.isRunning());
        assertFalse(node.getMining());
    }

    @Test
    public void testStart() {
        NodeApi nodeApi = apiClient.buildClient(NodeApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"attributes\":{\"result\":true}},\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<APIData<SimpleResult>, Boolean> res = nodeApi.start();
        assertNotNull(res);
        assertEquals(res.getData(), Boolean.TRUE);
    }

    @Test
    public void testStop() {
        NodeApi nodeApi = apiClient.buildClient(NodeApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"attributes\":{\"result\":true}},\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<APIData<SimpleResult>, Boolean> res = nodeApi.stop();
        assertNotNull(res);
        assertEquals(res.getData(), Boolean.TRUE);
    }

    @Test
    public void testRestart() {
        NodeApi nodeApi = apiClient.buildClient(NodeApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"attributes\":{\"result\":true}},\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<APIData<SimpleResult>, Boolean> res = nodeApi.restart();
        assertNotNull(res);
        assertEquals(res.getData(), Boolean.TRUE);
    }

    @Test
    public void testReset() {
        NodeApi nodeApi = apiClient.buildClient(NodeApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"attributes\":{\"result\":true}},\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<APIData<SimpleResult>, Boolean> res = nodeApi.reset();
        assertNotNull(res);
        assertEquals(res.getData(), Boolean.TRUE);
    }

}
