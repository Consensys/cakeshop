package com.jpmorgan.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class Peer   {

    private String status = null;
    private String id = null;
    private String nodeName = null;
    private String nodeUrl = null;
    private String nodeIP = null;


    /**
     * Status of the node, it has two values \"running\" or \"stopped\"
     **/
    public Peer status(String status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Status of the node, it has two values \"running\" or \"stopped\"")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    /**
     * Unique Node ID
     **/
    public Peer id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Unique Node ID")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     * Friendly node name (includes client and version info)
     **/
    public Peer nodeName(String nodeName) {
        this.nodeName = nodeName;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Friendly node name (includes client and version info)")
    @JsonProperty("nodeName")
    public String getNodeName() {
        return nodeName;
    }
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }


    /**
     * enode URI (Includes host IP and port number)
     **/
    public Peer nodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
        return this;
    }

    @ApiModelProperty(example = "null", value = "enode URI (Includes host IP and port number)")
    @JsonProperty("nodeUrl")
    public String getNodeUrl() {
        return nodeUrl;
    }
    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }


    /**
     * IP address of the node
     **/
    public Peer nodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
        return this;
    }

    @ApiModelProperty(example = "null", value = "IP address of the node")
    @JsonProperty("nodeIP")
    public String getNodeIP() {
        return nodeIP;
    }
    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Peer peer = (Peer) o;
        return Objects.equals(this.status, peer.status) &&
                Objects.equals(this.id, peer.id) &&
                Objects.equals(this.nodeName, peer.nodeName) &&
                Objects.equals(this.nodeUrl, peer.nodeUrl) &&
                Objects.equals(this.nodeIP, peer.nodeIP);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, id, nodeName, nodeUrl, nodeIP);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}

