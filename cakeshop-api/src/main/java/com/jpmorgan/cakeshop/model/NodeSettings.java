/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.model;

public class NodeSettings {

    private String identity, extraParams, genesisBlock, blockMakerAccount, voterAccount;
    private Integer minBlockTime, maxBlockTime, logLevel, networkId;
    private Boolean isMining;

    public NodeSettings() {

    }

    /**
     * @return the logLevel
     */
    public Integer getLogLevel() {
        return logLevel;
    }

    /**
     * @param logLevel the logLevel to set
     */
    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }

    public NodeSettings logLevel(Integer logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    /**
     * @return the networkId
     */
    public Integer getNetworkId() {
        return networkId;
    }

    /**
     * @param networkId the networkId to set
     */
    public void setNetworkId(Integer networkId) {
        this.networkId = networkId;
    }

    public NodeSettings networkId(Integer networkId) {
        this.networkId = networkId;
        return this;
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

    public NodeSettings identity(String identity) {
        this.identity = identity;
        return this;
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

    public NodeSettings extraParams(String extraParams) {
        this.extraParams = extraParams;
        return this;
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

    public NodeSettings genesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
        return this;
    }

    /**
     * @return the blockMakerAccount
     */
    public String getBlockMakerAccount() {
        return blockMakerAccount;
    }

    /**
     * @param blockMakerAccount the blockMakerAccount to set
     */
    public void setBlockMakerAccount(String blockMakerAccount) {
        this.blockMakerAccount = blockMakerAccount;
    }

    public NodeSettings blockMakerAccount(String blockMakerAccount) {
        this.blockMakerAccount = blockMakerAccount;
        return this;
    }

    /**
     * @return the voterAccount
     */
    public String getVoterAccount() {
        return voterAccount;
    }

    /**
     * @param voterAccount the voterAccount to set
     */
    public void setVoterAccount(String voterAccount) {
        this.voterAccount = voterAccount;
    }

    public NodeSettings voterAccount(String voterAccount) {
        this.voterAccount = voterAccount;
        return this;
    }

    /**
     * @return the minBlockTime
     */
    public Integer getMinBlockTime() {
        return minBlockTime;
    }

    /**
     * @param minBlockTime the minBlockTime to set
     */
    public void setMinBlockTime(Integer minBlockTime) {
        this.minBlockTime = minBlockTime;
    }

    public NodeSettings minBlockTime(Integer minBlockTime) {
        this.minBlockTime = minBlockTime;
        return this;
    }

    /**
     * @return the maxBlockTime
     */
    public Integer getMaxBlockTime() {
        return maxBlockTime;
    }

    /**
     * @param maxBlockTime the maxBlockTime to set
     */
    public void setMaxBlockTime(Integer maxBlockTime) {
        this.maxBlockTime = maxBlockTime;
    }

    public NodeSettings maxBlockTime(Integer maxBlockTime) {
        this.maxBlockTime = maxBlockTime;
        return this;
    }

    /**
     * @return the isMining
     */
    public Boolean isMining() {
        return isMining;
    }

    /**
     * @param isMining the isMining to set
     */
    public void setIsMining(Boolean isMining) {
        this.isMining = isMining;
    }

    public NodeSettings isMining(Boolean isMining) {
        this.isMining = isMining;
        return this;
    }

}
