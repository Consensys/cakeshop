package com.jpmorgan.cakeshop.service;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Account;

import java.util.List;


public interface WalletService {

    /**
     * List accounts in the wallet
     *
     * @return
     * @throws APIException
     */
    public List<Account> list() throws APIException;

    /**
     * Create new account (no passphrase for now)
     *
     * @return
     * @throws APIException
     */
    public Account create() throws APIException;

    /**
     * Test whether or not the given account is unlocked in the local wallet
     *
     * @param address
     * @return
     * @throws APIException
     */
    public boolean isUnlocked(String address) throws APIException;

}
