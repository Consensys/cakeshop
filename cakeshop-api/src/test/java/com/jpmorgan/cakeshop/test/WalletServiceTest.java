package com.jpmorgan.cakeshop.test;

import static org.testng.Assert.*;

import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.service.WalletService;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

@Test(singleThreaded = true)
public class WalletServiceTest extends BaseGethRpcTest {

    @Autowired
    private WalletService wallet;

    @Autowired
    private WalletDAO walletDAO;

    @Test(priority = 1)
    public void testList() throws APIException {
        System.out.println("Running  WalletServiceTest.testList-------------------------------------");
        List<Account> accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), 8);
        assertTrue(StringUtils.isNotBlank(accounts.get(0).getAddress()));
        assertEquals(walletDAO.list().size(), 8);
    }

    @Test(priority = 3)
    public void testCreate() throws APIException {

        List<Account> accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), 8);

        // create
        Account acc = wallet.create();
        assertNotNull(acc);
        assertTrue(StringUtils.isNotBlank(acc.getAddress()));

        accounts = wallet.list();
        assertNotNull(accounts);
        assertEquals(accounts.size(), 9);

        assertEquals(walletDAO.list().size(), 9);
    }

}
