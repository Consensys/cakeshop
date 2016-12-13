package com.jpmorgan.cakeshop.client.test.api;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.client.api.WalletApi;
import com.jpmorgan.cakeshop.client.model.Account;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

import java.util.List;

import org.testng.annotations.Test;

import okhttp3.mockwebserver.MockResponse;

public class WalletApiTest extends BaseApiTest {

    @Test
    public void testList() {
        WalletApi walletApi = apiClient.buildClient(WalletApi.class);

        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":[{\"id\":\"0x2e219248f44546d966808cdd20cb6c36df6efa82\",\"type\":\"wallet\",\"attributes\":{\"address\":\"0x2e219248f44546d966808cdd20cb6c36df6efa82\",\"balance\":\"1606938044258990275541962092341162602522222993782792835301376\"}},{\"id\":\"0xcd5b17da5ad176905c12fc85ce43ec287ab55363\",\"type\":\"wallet\",\"attributes\":{\"address\":\"0xcd5b17da5ad176905c12fc85ce43ec287ab55363\",\"balance\":\"1606938044258990275541962092341162602522202993782792835301376\"}},{\"id\":\"0x50bb02281de5f00cc1f1dd5a6692da3fa9b2d912\",\"type\":\"wallet\",\"attributes\":{\"address\":\"0x50bb02281de5f00cc1f1dd5a6692da3fa9b2d912\",\"balance\":\"1606938044258990275541962092341162602522202993782792835301376\"}},{\"id\":\"0x32e39729f4bc8cb0c01184aedfa13538f3f4d150\",\"type\":\"wallet\",\"attributes\":{\"address\":\"0x32e39729f4bc8cb0c01184aedfa13538f3f4d150\",\"balance\":\"0\"}}],\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<List<APIData<Account>>, Account> res = walletApi.list();

        assertNotNull(res);
        List<Account> accounts = res.getDataAsList();
        assertEquals(accounts.size(), 4);
    }

    @Test
    public void testCreate() {
        WalletApi walletApi = apiClient.buildClient(WalletApi.class);

        mockWebServer.enqueue(new MockResponse().setBody("{\"data\":{\"id\":\"0xfda90e18e62b7ac06868223bd8f9ea79bde83874\",\"type\":\"wallet\",\"attributes\":{\"address\":\"0xfda90e18e62b7ac06868223bd8f9ea79bde83874\",\"balance\":null}},\"meta\":{\"version\":\"1.0\"}}"));
        APIResponse<APIData<Account>, Account> res = walletApi.create();
        Account account = res.getData();
        assertNotNull(account);
        assertNotNull(account.getAddress());
        assertNull(account.getBalance());
    }

}
