/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.model.json;

public class NodePostJsonRequest {

    private String address;
    private String logLevel, networkId, identity, genesisBlock, extraParams, transactionManagerNode;
    private Object committingTransactions;
    private boolean raftLearner, istanbulPropose;

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the logLevel
     */
    public String getLogLevel() {
        return logLevel;
    }

    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * @return the networkId
     */
    public String getNetworkId() {
        return networkId;
    }

    /**
     * @param networkId the networkId to set
     */
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    /**
     * @return the identity
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * @param identity the identity to set
     */
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    /**
     * @return the genesisBlock
     */
    public String getGenesisBlock() {
        return genesisBlock;
    }

    /**
     * @param genesisBlock the genesisBlock to set
     */
    public void setGenesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
    }

    /**
     * @return the committingTransactions
     */
    public Object getCommittingTransactions() {
        return committingTransactions;
    }

    /**
     * @param committingTransactions the committingTransactions to set
     */
    public void setCommittingTransactions(Object committingTransactions) {
        this.committingTransactions = committingTransactions;
    }

    /**
     * @return the extraParams
     */
    public String getExtraParams() {
        return extraParams;
    }

    /**
     * @param extraParams the extraParams to set
     */
    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }

    /**
     * @return the transactionManagerNode
     */
    public String getTransactionManagerNode() {
        return transactionManagerNode;
    }

    /**
     * @param transactionManagerNode the constellationNode to set
     */
    public void setTransactionManagerNode(String transactionManagerNode) {
        this.transactionManagerNode = transactionManagerNode;
    }

    public boolean isRaftLearner() {
        return raftLearner;
    }

    public void setRaftLearner(boolean raftLearner) {
        this.raftLearner = raftLearner;
    }
    
    public boolean isIstanbulPropose() {
        return istanbulPropose;
    }

    public void setIstanbulPropose(boolean istanbulPropose) {
        this.istanbulPropose = istanbulPropose;
    }
}
