package com.jpmorgan.cakeshop.manager.model.json;

public class NodeJsonRequest {

    private String cakeshopUrl, cred1, cred2, logLevel, genesisBlock, extraParams, networkId,
            blockMakerAccount, voterAccount, constellationNode, address;
    private Boolean committingTransactions;
    private Integer minBlockTime, maxBlockTime;

    /**
     * @return the cakeshopUrl
     */
    public String getCakeshopUrl() {
        return cakeshopUrl;
    }

    /**
     * @param cakeshopUrl the cakeshopUrl to set
     */
    public void setCakeshopUrl(String cakeshopUrl) {
        this.cakeshopUrl = cakeshopUrl;
    }

    /**
     * @return the cred1
     */
    public String getCred1() {
        return cred1;
    }

    /**
     * @param cred1 the cred1 to set
     */
    public void setCred1(String cred1) {
        this.cred1 = cred1;
    }

    /**
     * @return the cred2
     */
    public String getCred2() {
        return cred2;
    }

    /**
     * @param cred2 the cred2 to set
     */
    public void setCred2(String cred2) {
        this.cred2 = cred2;
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
     * @return the committingTransactions
     */
    public Boolean getCommittingTransactions() {
        return committingTransactions;
    }

    /**
     * @param committingTransactions the committingTransactions to set
     */
    public void setCommittingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
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
