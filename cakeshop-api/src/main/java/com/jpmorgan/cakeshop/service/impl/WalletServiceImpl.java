package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.json.WalletPostJsonRequest;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.GethRpcConstants;
import com.jpmorgan.cakeshop.service.WalletService;
import com.jpmorgan.cakeshop.util.AbiUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jpmorgan.cakeshop.util.CakeshopUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Samer Falah
 */
@Service
public class WalletServiceImpl implements WalletService, GethRpcConstants {

    private static final String DUMMY_PAYLOAD_HASH = AbiUtils.sha3AsHex("foobar");
    private static final Logger LOG = LoggerFactory.getLogger(WalletServiceImpl.class);

    @Autowired
    private GethHttpService gethService;

  @SuppressWarnings("unchecked")
    @Override
    public List<Account> list() throws APIException {

        List<String> accountList = null;
        List<Account> accounts = null;
        Account account = null;

        Map<String, Object> data = gethService.executeGethCall(PERSONAL_LIST_ACCOUNTS, new Object[]{});

        if (data != null && data.containsKey(CakeshopUtils.SIMPLE_RESULT)) {
            accountList = (List<String>) data.get(CakeshopUtils.SIMPLE_RESULT);
            if (accountList != null) {
                accounts = new ArrayList<>();
                for (String address : accountList) {
                    Map<String, Object> accountData = gethService.executeGethCall(
                            PERSONAL_GET_ACCOUNT_BALANCE, new Object[]{address, "latest"});
                    String strBal = (String) accountData.get(CakeshopUtils.SIMPLE_RESULT);
                    BigInteger bal = AbiUtils.hexToBigInteger(strBal);
                    account = new Account();
                    account.setAddress(address);
                    account.setBalance(bal.toString());
                    account.setUnlocked(isUnlocked(address));
                    accounts.add(account);

                }
            }
        }

        return accounts;
    }

    @Override
    public Account create() throws APIException {
        Map<String, Object> result = gethService.executeGethCall("personal_newAccount", new Object[]{""});
        String newAddress = (String) result.get(CakeshopUtils.SIMPLE_RESULT);

        Account account = new Account();
        account.setAddress(newAddress);

        return account;
    }

    @Override
    public Boolean unlockAccount(WalletPostJsonRequest request) throws APIException {
        try {
            // pass in 0 to unlock indefinitely
            Map<String, Object> result = gethService.executeGethCall("personal_unlockAccount", new Object[]{request.getAccount(), request.getAccountPassword(), 0});
            String response = result.get(CakeshopUtils.SIMPLE_RESULT).toString();
            if (StringUtils.isNotBlank(response) && Boolean.valueOf(response)) {
                return true;
            }
        } catch (APIException ex) {
            throw ex;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean lockAccount(WalletPostJsonRequest request) throws APIException {
        try {
            Map<String, Object> result = gethService.executeGethCall("personal_lockAccount", new Object[]{request.getAccount()});
            String response = result.get(CakeshopUtils.SIMPLE_RESULT).toString();
            if (StringUtils.isNotBlank(response) && Boolean.valueOf(response)) {
                return Boolean.TRUE;
            }
        } catch (APIException ex) {
            throw ex;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean fundAccount(WalletPostJsonRequest request) throws APIException {
        try {
            String accountFrom = StringUtils.isNotBlank(request.getFromAccount()) ? request.getFromAccount()
                    : list().get(0).getAddress();
            if (accountFrom.equals(request.getAccount())) {
                accountFrom = list().get(1).getAddress();
            }
            Map<String, Object> fundArgs = new HashMap<>();
            fundArgs.put("from", accountFrom);
            fundArgs.put("to", request.getAccount());
            fundArgs.put("value", AbiUtils.toHexWithNoLeadingZeros(request.getNewBalance()));
            
            Map<String, Object> result = gethService.executeGethCall("eth_sendTransaction", new Object[]{fundArgs});
            String response = result.get(CakeshopUtils.SIMPLE_RESULT).toString();
            if (StringUtils.isNotBlank(response)) {
                return Boolean.TRUE;
            }
        } catch (APIException ex) {
            throw ex;
        }
        return Boolean.FALSE;

    }

    @Override
    public boolean isUnlocked(String address) throws APIException {
        try {
            Map<String, Object> result = gethService.executeGethCall("eth_sign", new Object[]{address, "0x" + DUMMY_PAYLOAD_HASH});
            if (StringUtils.isNotBlank((String) result.get(CakeshopUtils.SIMPLE_RESULT))) {
                return true;
            }
        } catch (APIException e) {
            if (!e.getMessage().contains("authentication needed: password or unlock")) {
                throw e;
            }
        }
        return false;
    }
}
