package com.jpmorgan.cakeshop.model.json;

import java.math.BigInteger;

public class WalletPostJsonRequest {

    private String fromAccount, account, accountPassword;
    private BigInteger newBalance;

    /**
     * @return the formAccount
     */
    public String getFromAccount() {
        return fromAccount;
    }

    /**
     * @param fromAccount the formAccount to set
     */
    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    /**
     * @return the account
     */
    public String getAccount() {
        return account;
    }

    /**
     * @param account the account to set
     */
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * @return the accountPassword
     */
    public String getAccountPassword() {
        return accountPassword;
    }

    /**
     * @param accountPassword the accountPassword to set
     */
    public void setAccountPassword(String accountPassword) {
        this.accountPassword = accountPassword;
    }

    /**
     * @return the newBalance
     */
    public BigInteger getNewBalance() {
        return newBalance;
    }

    /**
     * @param newBalance the newBalance to set
     */
    public void setNewBalance(BigInteger newBalance) {
        this.newBalance = newBalance;
    }
}
