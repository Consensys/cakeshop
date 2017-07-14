package com.jpmorgan.cakeshop.client.model.req;

public class NodeUpdateCommand {

    private String logLevel, genesisBlock, extraParams, networkId, blockMakerAccount, voterAccount, constellationNode, address;
    private Boolean committingTransactions;
    private Integer minBlockTime, maxBlockTime;

    public NodeUpdateCommand() {
    }

    public NodeUpdateCommand logLevel(String logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public NodeUpdateCommand networkId(String networkId) {
        this.networkId = networkId;
        return this;
    }

    public NodeUpdateCommand commitingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
        return this;
    }

    public NodeUpdateCommand extraParams(String extraParams) {
        this.extraParams = extraParams;
        return this;
    }

    public NodeUpdateCommand genesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
        return this;
    }

    public NodeUpdateCommand blockMakerAccount(String blockMakerAccount) {
        this.blockMakerAccount = blockMakerAccount;
        return this;
    }

    public NodeUpdateCommand voterAccount(String voterAccount) {
        this.voterAccount = voterAccount;
        return this;
    }

    public NodeUpdateCommand constellationNode(String constellationNode) {
        this.constellationNode = constellationNode;
        return this;
    }

    public NodeUpdateCommand address(String address) {
        this.address = address;
        return this;
    }

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

    public NodeUpdateCommand minBlockTime(Integer minBlockTime) {
        this.minBlockTime = minBlockTime;
        return this;
    }

    public NodeUpdateCommand maxBlockTime(Integer maxBlockTime) {
        this.maxBlockTime = maxBlockTime;
        return this;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public Boolean getCommittingTransactions() {
        return committingTransactions;
    }

    public void setCommittingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
    }

    public String getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }

    public String getGenesisBlock() {
        return genesisBlock;
    }

    public void setGenesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
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

    /**
     * @return the constellationNode
     */
    public String getConstellationNode() {
        return constellationNode;
    }

    /**
     * @param constellationNode the constellationNode to set
     */
    public void setConstellationNode(String constellationNode) {
        this.constellationNode = constellationNode;
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

}
