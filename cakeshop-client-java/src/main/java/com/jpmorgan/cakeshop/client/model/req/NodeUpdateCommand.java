package com.jpmorgan.cakeshop.client.model.req;

public class NodeUpdateCommand {

    private String logLevel;
    private String networkId;
    private Boolean committingTransactions;
    private String extraParams;
    private String genesisBlock;

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

}
