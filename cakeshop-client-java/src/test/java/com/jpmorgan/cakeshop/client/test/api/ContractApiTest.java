package com.jpmorgan.cakeshop.client.test.api;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.client.api.ContractApi;
import com.jpmorgan.cakeshop.client.model.Contract;
import com.jpmorgan.cakeshop.client.model.Transaction;
import com.jpmorgan.cakeshop.client.model.TransactionResult;
import com.jpmorgan.cakeshop.client.model.Contract.CodeTypeEnum;
import com.jpmorgan.cakeshop.client.model.req.ContractCompileCommand;
import com.jpmorgan.cakeshop.client.model.req.ContractCreateCommand;
import com.jpmorgan.cakeshop.client.model.req.ContractMethodCallCommand;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

import java.util.List;

import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;

public class ContractApiTest extends BaseApiTest {

    private final String code = "contract SimpleStorage { uint public storedData; function SimpleStorage(uint initVal) { storedData = initVal; } function set(uint x) { storedData = x; } function get() constant returns (uint retVal) { return storedData; } }";
    private final String contractAddress = "0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2";

    @Test
    public void testList() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);

        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":[{\"id\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"type\":\"contract\",\"attributes\":{\"address\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"SimpleStorage\",\"owner\":null,\"code\":\"\\ncontract SimpleStorage {\\n\\n    uint public storedData;\\n\\n    function SimpleStorage(uint initVal) {\\n        storedData = initVal;\\n    }\\n\\n    function set(uint x) {\\n        storedData = x;\\n    }\\n\\n    function get() constant returns (uint retVal) {\\n        return storedData;\\n    }\\n    \\n}\\n\",\"codeType\":\"solidity\",\"binary\":null,\"abi\":\"[{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"storedData\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"x\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"set\\\",\\\"outputs\\\":[],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"get\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"retVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"initVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"constructor\\\"}]\\n\",\"createdDate\":1464365329,\"gasEstimates\":null,\"solidityInterface\":null,\"functionHashes\":null}}],\"meta\":{\"version\":\"1.0\"}}"));
        List<Contract> contracts = contractApi.list().getDataAsList();
        assertNotNull(contracts);
        assertEquals(contracts.size(), 1);

        Contract contract = contracts.get(0);
        commonTests(contract);

    }

    @Test
    public void testGet() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"type\":\"contract\",\"attributes\":{\"address\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"SimpleStorage\",\"owner\":null,\"code\":\"\\ncontract SimpleStorage {\\n\\n    uint public storedData;\\n\\n    function SimpleStorage(uint initVal) {\\n        storedData = initVal;\\n    }\\n\\n    function set(uint x) {\\n        storedData = x;\\n    }\\n\\n    function get() constant returns (uint retVal) {\\n        return storedData;\\n    }\\n    \\n}\\n\",\"codeType\":\"solidity\",\"binary\":\"0x60606040526000357c0100000000000000000000000000000000000000000000000000000000900480632a1afcd914604b57806360fe47b114606c5780636d4ce63c146082576049565b005b6056600480505060c2565b6040518082815260200191505060405180910390f35b6080600480803590602001909190505060a3565b005b608d600480505060b1565b6040518082815260200191505060405180910390f35b806000600050819055505b50565b6000600060005054905060bf565b90565b6000600050548156\",\"abi\":\"[{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"storedData\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"x\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"set\\\",\\\"outputs\\\":[],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"get\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"retVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"initVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"constructor\\\"}]\\n\",\"createdDate\":1464365329,\"gasEstimates\":null,\"solidityInterface\":null,\"functionHashes\":null}},\"meta\":{\"version\":\"1.0\"}}"));
        Contract c = contractApi.get("0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2").getData();
        commonTests(c);
    }

    private void commonTests(Contract contract) {
        assertNotNull(contract);
        assertEquals(contract.getCodeType(), CodeTypeEnum.SOLIDITY);
        assertEquals(contract.getCreatedDate(), Long.valueOf(1464365329));
        assertEquals(contract.getAddress(), "0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2");
    }

    @Test
    public void testCompile() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":[{\"type\":\"contract\",\"attributes\":{\"address\":null,\"name\":\"SimpleStorage\",\"owner\":null,\"code\":\"contract SimpleStorage { uint public storedData; function SimpleStorage(uint initVal) { storedData = initVal; } function set(uint x) { storedData = x; } function get() constant returns (uint retVal) { return storedData; } }\",\"codeType\":\"solidity\",\"binary\":\"6060604052604051602080610105833981016040528080519060200190919050505b806000600050819055505b5060cb8061003a6000396000f360606040526000357c0100000000000000000000000000000000000000000000000000000000900480632a1afcd914604b57806360fe47b114606c5780636d4ce63c146082576049565b005b6056600480505060a3565b6040518082815260200191505060405180910390f35b6080600480803590602001909190505060ac565b005b608d600480505060ba565b6040518082815260200191505060405180910390f35b60006000505481565b806000600050819055505b50565b6000600060005054905060c8565b9056\",\"abi\":\"[{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"storedData\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":false,\\\"inputs\\\":[{\\\"name\\\":\\\"x\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"name\\\":\\\"set\\\",\\\"outputs\\\":[],\\\"type\\\":\\\"function\\\"},{\\\"constant\\\":true,\\\"inputs\\\":[],\\\"name\\\":\\\"get\\\",\\\"outputs\\\":[{\\\"name\\\":\\\"retVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"function\\\"},{\\\"inputs\\\":[{\\\"name\\\":\\\"initVal\\\",\\\"type\\\":\\\"uint256\\\"}],\\\"type\\\":\\\"constructor\\\"}]\\n\",\"createdDate\":1464711329,\"gasEstimates\":{\"creation\":[20159,40600],\"external\":{\"get()\":269,\"set(uint256)\":20160,\"storedData()\":205},\"internal\":{}},\"solidityInterface\":\"contract SimpleStorage{function SimpleStorage(uint256 initVal);function storedData()constant returns(uint256 );function set(uint256 x);function get()constant returns(uint256 retVal);}\",\"functionHashes\":{\"get()\":\"6d4ce63c\",\"set(uint256)\":\"60fe47b1\",\"storedData()\":\"2a1afcd9\"}}}],\"meta\":{\"version\":\"1.0\"}}"));
        List<Contract> contracts = contractApi.compile(new ContractCompileCommand().code(code)).getDataAsList();
        assertNotNull(contracts);
        assertEquals(contracts.size(), 1);
        Contract c = contracts.get(0);
        assertEquals(c.getName(), "SimpleStorage");
        assertNotNull(c.getGasEstimates().get("creation"));
    }

    @Test
    public void testCreate() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"0xad82ec33379c1b083e6a9dc4ef5ef3f9940f0004c240040d5eb049f262614804\",\"type\":\"transaction_result\",\"attributes\":{\"id\":\"0xad82ec33379c1b083e6a9dc4ef5ef3f9940f0004c240040d5eb049f262614804\"}},\"meta\":{\"version\":\"1.0\"}}"));
        TransactionResult tr  = contractApi.create(new ContractCreateCommand().from("0x2e219248f44546d966808cdd20cb6c36df6efa82").code(code)).getData();
        assertNotNull(tr);
        assertEquals(tr.getId(), "0xad82ec33379c1b083e6a9dc4ef5ef3f9940f0004c240040d5eb049f262614804");
    }

    @Test
    public void testListTransactions() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":[{\"id\":\"0xcb4c6370188d2f0abb05ab60440f5979e96dca412ad5e3c308fa75685ed5ada2\",\"type\":\"transaction\",\"attributes\":{\"id\":\"0xcb4c6370188d2f0abb05ab60440f5979e96dca412ad5e3c308fa75685ed5ada2\",\"status\":\"committed\",\"nonce\":\"0x1\",\"blockId\":\"0xa3276e99ea787f53a35f242dcf87a91071ebf776b7f24fb9058caf427568e5bc\",\"blockNumber\":861,\"transactionIndex\":0,\"from\":\"0x2e219248f44546d966808cdd20cb6c36df6efa82\",\"to\":null,\"value\":0,\"gas\":10000000,\"gasPrice\":20000000000,\"input\":\"0x60606040526040516020806101f7833981016040528080519060200190919050505b7f3c5ad147104e56be34a9176a6692f7df8d2f4b29a5af06bc6b98970d329d65778160405180806020018381526020018281038252600c8152602001807f696e69742073746f7261676500000000000000000000000000000000000000008152602001506020019250505060405180910390a1806000600050819055505b50610149806100ae6000396000f360606040526000357c0100000000000000000000000000000000000000000000000000000000900480632a1afcd91461004f57806360fe47b1146100725780636d4ce63c1461008a5761004d565b005b61005c60048050506100ad565b6040518082815260200191505060405180910390f35b61008860048080359060200190919050506100b6565b005b6100976004805050610137565b6040518082815260200191505060405180910390f35b60006000505481565b7f3c5ad147104e56be34a9176a6692f7df8d2f4b29a5af06bc6b98970d329d65778160405180806020018381526020018281038252600a8152602001807f6368616e67652076616c000000000000000000000000000000000000000000008152602001506020019250505060405180910390a1806000600050819055505b50565b60006000600050549050610146565b9056000000000000000000000000000000000000000000000000000000000000000b\",\"decodedInput\":null,\"cumulativeGasUsed\":137259,\"gasUsed\":137259,\"contractAddress\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"logs\":[{\"id\":1,\"blockId\":\"0xa3276e99ea787f53a35f242dcf87a91071ebf776b7f24fb9058caf427568e5bc\",\"blockNumber\":861,\"logIndex\":0,\"transactionId\":\"0xcb4c6370188d2f0abb05ab60440f5979e96dca412ad5e3c308fa75685ed5ada2\",\"contractId\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"Debug\",\"data\":[\"init storage\",11]}]}},{\"id\":\"0x530b00addcdad961523d513f36b60682df881b3d0cd4bd47a88b2e508eeeccb0\",\"type\":\"transaction\",\"attributes\":{\"id\":\"0x530b00addcdad961523d513f36b60682df881b3d0cd4bd47a88b2e508eeeccb0\",\"status\":\"committed\",\"nonce\":\"0x3\",\"blockId\":\"0x3f508c903191f1b9c15143f74836c9c0056b823595565e8c7d565690f38ed69f\",\"blockNumber\":863,\"transactionIndex\":0,\"from\":\"0x2e219248f44546d966808cdd20cb6c36df6efa82\",\"to\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"value\":0,\"gas\":10000000,\"gasPrice\":20000000000,\"input\":\"0x60fe47b1000000000000000000000000000000000000000000000000000000000000000c\",\"decodedInput\":{\"method\":\"set\",\"args\":[12]},\"cumulativeGasUsed\":28532,\"gasUsed\":28532,\"contractAddress\":null,\"logs\":[{\"id\":2,\"blockId\":\"0x3f508c903191f1b9c15143f74836c9c0056b823595565e8c7d565690f38ed69f\",\"blockNumber\":863,\"logIndex\":0,\"transactionId\":\"0x530b00addcdad961523d513f36b60682df881b3d0cd4bd47a88b2e508eeeccb0\",\"contractId\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"Debug\",\"data\":[\"change val\",12]}]}},{\"id\":\"0xab07d7f5a44b3709f0183fd783ff6cb9a2274d33bfd6115493bcd6147f6574d6\",\"type\":\"transaction\",\"attributes\":{\"id\":\"0xab07d7f5a44b3709f0183fd783ff6cb9a2274d33bfd6115493bcd6147f6574d6\",\"status\":\"committed\",\"nonce\":\"0x4\",\"blockId\":\"0x4060394e85c748c4113c7dcdfef213ed28d93bd765403fa29d564412b8f4375d\",\"blockNumber\":864,\"transactionIndex\":0,\"from\":\"0x2e219248f44546d966808cdd20cb6c36df6efa82\",\"to\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"value\":0,\"gas\":10000000,\"gasPrice\":20000000000,\"input\":\"0x60fe47b1000000000000000000000000000000000000000000000000000000000000000b\",\"decodedInput\":{\"method\":\"set\",\"args\":[11]},\"cumulativeGasUsed\":28532,\"gasUsed\":28532,\"contractAddress\":null,\"logs\":[{\"id\":3,\"blockId\":\"0x4060394e85c748c4113c7dcdfef213ed28d93bd765403fa29d564412b8f4375d\",\"blockNumber\":864,\"logIndex\":0,\"transactionId\":\"0xab07d7f5a44b3709f0183fd783ff6cb9a2274d33bfd6115493bcd6147f6574d6\",\"contractId\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"Debug\",\"data\":[\"change val\",11]}]}},{\"id\":\"0xb25c3876f49c29575356d7407630edd5441e1a16a7f2a48c3010d36a31df7356\",\"type\":\"transaction\",\"attributes\":{\"id\":\"0xb25c3876f49c29575356d7407630edd5441e1a16a7f2a48c3010d36a31df7356\",\"status\":\"committed\",\"nonce\":\"0x21\",\"blockId\":\"0x270d64ff954afa022bcb1e7e369ec81c13c5306c48b4cb4a96b8f638f548982c\",\"blockNumber\":893,\"transactionIndex\":0,\"from\":\"0x2e219248f44546d966808cdd20cb6c36df6efa82\",\"to\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"value\":0,\"gas\":10000000,\"gasPrice\":20000000000,\"input\":\"0x60fe47b1000000000000000000000000000000000000000000000000000000000000000c\",\"decodedInput\":{\"method\":\"set\",\"args\":[12]},\"cumulativeGasUsed\":28532,\"gasUsed\":28532,\"contractAddress\":null,\"logs\":[{\"id\":9,\"blockId\":\"0x270d64ff954afa022bcb1e7e369ec81c13c5306c48b4cb4a96b8f638f548982c\",\"blockNumber\":893,\"logIndex\":0,\"transactionId\":\"0xb25c3876f49c29575356d7407630edd5441e1a16a7f2a48c3010d36a31df7356\",\"contractId\":\"0x881ba6a77748bf1d54b7cb653eabba9a1bc7a0a2\",\"name\":\"Debug\",\"data\":[\"change val\",12]}]}}],\"meta\":{\"version\":\"1.0\"}}"));
        List<Transaction> txns = contractApi.listTransactions(contractAddress).getDataAsList();
        assertNotNull(txns);
        assertEquals(txns.size(), 4);
        Transaction tx = txns.get(0);
        assertEquals(tx.getContractAddress(), contractAddress);
        assertEquals(tx.getStatus(), "committed");
    }

    @Test
    public void testRead() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":[12],\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<List<Object>, Object> ret = contractApi.read(new ContractMethodCallCommand().address(contractAddress).method("get"));
        List<Object> res = ret.getApiData();
        assertEquals(res.get(0), 12);
    }

    @Test
    public void testTransact() {
        ContractApi contractApi = apiClient.buildClient(ContractApi.class);
        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"0xe99819e37c0c92fc97653832275f5ba637547eeddc2c3e44b7eed9d955427a16\",\"type\":\"transaction_result\",\"attributes\":{\"id\":\"0xe99819e37c0c92fc97653832275f5ba637547eeddc2c3e44b7eed9d955427a16\"}},\"meta\":{\"version\":\"1.0\"}}"));
        TransactionResult tr = contractApi.transact(new ContractMethodCallCommand().address(contractAddress).method("set").args(new Object[]{ 100 })).getData();
        assertNotNull(tr);
        assertEquals(tr.getId(), "0xe99819e37c0c92fc97653832275f5ba637547eeddc2c3e44b7eed9d955427a16");
    }

}
