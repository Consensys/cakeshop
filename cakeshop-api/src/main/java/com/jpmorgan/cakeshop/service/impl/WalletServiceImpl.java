package com.jpmorgan.cakeshop.service.impl;

import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.json.WalletPostJsonRequest;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.GethRpcConstants;
import com.jpmorgan.cakeshop.service.WalletService;
import com.jpmorgan.cakeshop.util.AbiUtils;

import java.io.IOException;
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
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.Transaction;

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

    @Autowired
    private WalletDAO walletDAO;

    @SuppressWarnings("unchecked")
    @Override
    public List<Account> list() throws APIException {

        List<String> accountList = null;
        List<Account> accounts = null;
        Account account = null;
        
        try {
        	accountList = gethService.getAdminService().personalListAccounts().send().getAccountIds();
        } catch (IOException e) {
        	throw new APIException(e.getMessage());
        }
        
        if (accountList != null) {
        	accounts = new ArrayList<>();
        	for (String address : accountList) {
        		BigInteger balance = null;
        		try {
        			balance = gethService.getAdminService().ethGetBalance(address, DefaultBlockParameter.valueOf("latest")).send().getBalance();
        		} catch (IOException e) {
        			throw new APIException(e.getMessage());
        		}
        		if (balance == null) {
        			throw new APIException("unable to get balance");
        		}
        		account = new Account();
        		account.setAddress(address);
        		account.setBalance(balance.toString());
        		account.setUnlocked(isUnlocked(address));
        		accounts.add(account);

        	}
        }

        return accounts;
    }

    @Override
    public Account create() throws APIException {
    	String acctId = null;
    	try {
    		acctId = gethService.getAdminService().personalNewAccount("").send().getAccountId();
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	if (acctId == null) {
    		throw new APIException("new account failed");
    	}
        Account account = new Account();
        account.setAddress(acctId);
        walletDAO.save(account);

        return account;
    }

    @Override
    public Boolean unlockAccount(WalletPostJsonRequest request) throws APIException {
    	// pass in 0 to unlock indefinitely
    	boolean res = false;
    	try {
    		res = gethService.getAdminService().personalUnlockAccount(request.getAccount(), request.getAccountPassword(), BigInteger.ZERO).send().accountUnlocked();
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	return res;
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
        String accountFrom = StringUtils.isNotBlank(request.getFromAccount()) ? request.getFromAccount()
                : list().get(0).getAddress();
        if (accountFrom.equals(request.getAccount())) {
            accountFrom = list().get(1).getAddress();
        }
    	Transaction t = new Transaction(accountFrom, null, null, null, request.getAccount(), request.getNewBalance(), null);
    	String hash = null;
    	try {
    		hash = gethService.getQuorumService().ethSendTransaction(t).send().getTransactionHash();
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	}
    	if (hash == null) {
    		throw new APIException("fund account failure");
    	}
    	return (StringUtils.isNotBlank(hash));

    }

    @Override
    public boolean isUnlocked(String address) throws APIException {
    	try {
    		String sig = gethService.getQuorumService().ethSign(address, "0x" + DUMMY_PAYLOAD_HASH).send().getSignature();
    		return StringUtils.isNotBlank(sig);
    	} catch (APIException ex) {
            if (!ex.getMessage().contains("authentication needed: password or unlock")) {
            	throw ex;
            }
    	} catch (IOException e) {
    		throw new APIException(e.getMessage());
    	} 
    	return false;
    }
}
