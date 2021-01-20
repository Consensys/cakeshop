package com.jpmorgan.cakeshop.test;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.json.WalletPostJsonRequest;
import com.jpmorgan.cakeshop.service.WalletService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

@Test(singleThreaded = true)
public class WalletServiceTest extends BaseGethRpcTest {

    @Autowired
    private WalletService wallet;

    private int startingAccounts;

    @BeforeClass
    private void beforeAll() throws Exception {
        startingAccounts = wallet.list().size();
    }

    @Test(priority = 1)
    public void testList() throws APIException {
        System.out.println("Running  WalletServiceTest.testList-------------------------------------");
        List<Account> accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), startingAccounts);
        assertTrue(StringUtils.isNotBlank(accounts.get(0).getAddress()));
    }

    @Test(priority = 3)
    public void testCreate() throws APIException {

        List<Account> accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), startingAccounts);

        // create
        Account acc = wallet.create();
        assertNotNull(acc);
        assertTrue(StringUtils.isNotBlank(acc.getAddress()));

        WalletPostJsonRequest request = new WalletPostJsonRequest();
        request.setAccount(acc.getAddress());
        request.setAccountPassword("");
        wallet.unlockAccount(request);

        accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), startingAccounts + 1);
    }

}
