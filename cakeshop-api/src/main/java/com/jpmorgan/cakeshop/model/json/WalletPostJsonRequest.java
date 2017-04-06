package com.jpmorgan.cakeshop.model.json;

public class WalletPostJsonRequest {

    private String fromAccount, account, accountPassword;
    private Long newBalance;

    /**
     * @return the formAccount
     */
    public String getFromAccount() {
        return fromAccount;
    }

    /**
     * @param formAccount the formAccount to set
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
    public Long getNewBalance() {
        return newBalance;
    }

    /**
     * @param newBalance the newBalance to set
     */
    public void setNewBalance(Long newBalance) {
        this.newBalance = newBalance;
    }

}
