package com.jpmorgan.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class NodeConfig   {

    private String identity = null;
    private Boolean committingTransactions = null;
    private Integer networkId = null;
    private Integer logLevel = null;
    private String genesisBlock = null;
    private String extraParams = null;


    /**
     * Friendly node name which gets included in the full node Name
     **/
    public NodeConfig identity(String identity) {
        this.identity = identity;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Friendly node name which gets included in the full node Name")
    @JsonProperty("identity")
    public String getIdentity() {
        return identity;
    }
    public void setIdentity(String identity) {
        this.identity = identity;
    }


    /**
     * Indicates whether the node is commiting transactions or not
     **/
    public NodeConfig committingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Indicates whether the node is commiting transactions or not")
    @JsonProperty("committingTransactions")
    public Boolean getCommittingTransactions() {
        return committingTransactions;
    }
    public void setCommittingTransactions(Boolean committingTransactions) {
        this.committingTransactions = committingTransactions;
    }


    /**
     * Network identifier
     **/
    public NodeConfig networkId(Integer networkId) {
        this.networkId = networkId;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Network identifier")
    @JsonProperty("networkId")
    public Integer getNetworkId() {
        return networkId;
    }
    public void setNetworkId(Integer networkId) {
        this.networkId = networkId;
    }


    /**
     * Logging verbosity: 0-6 (0=silent, 1=error, 2=warn, 3=info, 4=core, 5=debug, 6=debug detail)
     **/
    public NodeConfig logLevel(Integer logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Logging verbosity: 0-6 (0=silent, 1=error, 2=warn, 3=info, 4=core, 5=debug, 6=debug detail)")
    @JsonProperty("logLevel")
    public Integer getLogLevel() {
        return logLevel;
    }
    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }

    public NodeConfig genesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
        return this;
    }
    @JsonProperty("genesisBlock")
    public String getGenesisBlock() {
        return genesisBlock;
    }
    public void setGenesisBlock(String genesisBlock) {
        this.genesisBlock = genesisBlock;
    }

    public NodeConfig extraParams(String extraParams) {
        this.extraParams = extraParams;
        return this;
    }
    @JsonProperty("extraParams")
    public String getExtraParams() {
        return extraParams;
    }
    public void setExtraParams(String extraParams) {
        this.extraParams = extraParams;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeConfig nodeInfo = (NodeConfig) o;
        return Objects.equals(this.identity, nodeInfo.identity) &&
                Objects.equals(this.committingTransactions, nodeInfo.committingTransactions) &&
                Objects.equals(this.networkId, nodeInfo.networkId) &&
                Objects.equals(this.logLevel, nodeInfo.logLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identity, committingTransactions, networkId, logLevel);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}

