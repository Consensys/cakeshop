package com.jpmorgan.cakeshop.model;

import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 *
 * @author Michael Kazansky
 */
public class NodeConfig {

    private String identity;
    private Boolean committingTransactions;
    private Integer networkId;
    private Integer logLevel;
    private String genesisBlock;
    private String extraParams;

    public NodeConfig (String identity, Boolean mining, Integer networkid, Integer verbosity, String genesisBlock, String extraParams) {
        this.identity = identity;
        this.committingTransactions = mining;
        this.networkId = networkid;
        this.logLevel = verbosity;
        this.setGenesisBlock(genesisBlock);
        this.setExtraParams(extraParams);
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
     * @return the mining
     */
    public Boolean getCommittingTransactions() {
        return committingTransactions;
    }

    /**
     * @param committingTransactions the mining to set
     */
    public void setCommittingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
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
    public void setNetworkid(Integer networkId) {
        this.networkId = networkId;
    }

    /**
     * @return the logLevel
     */
    public Integer getLogLevel() {
        return logLevel;
    }

    /**
     * @param logLevel the verbosity to set
     */
    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }

    public String getGenesisBlock() {
        return genesisBlock;
    }

    public void setGenesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
    }

    public String getExtraParams() {
        return extraParams;
    }

    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(null);
        data.setType("node-info");
        data.setAttributes(this);
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
