package com.jpmorgan.cakeshop.model;


import com.jpmorgan.cakeshop.util.StringUtils;

import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Node  {

    /**
     * Node status
     */
    private String status;

    /**
     * Node ID
     */
    private String id;

    /**
     * Number of peers connected to node
     */
    private int peerCount;

    /**
     * Last mined block
     */
    private long latestBlock;

    /**
     * Pending transactions in txpool
     */
    private long pendingTxn;

    /**
     * True if miner is running on the node
     */
    private boolean mining;

    /**
     * Node Address
     */
    private String nodeUrl;

    /**
     * Node Name
     */
    private String nodeName;

    /**
     * Node IP
     */
    private String nodeIP;

    /**
     * RPC URL
     */
    private String rpcUrl;

    /**
     * Ethereum data directory
     */
    private String dataDirectory;

    /**
     * Node configuration
     */
    private NodeConfig config;

    /**
     * Quorum configuration, if available
     */
    private QuorumInfo quorumInfo;

    /**
     * Connected peer list
     */
    private List<Peer> peers;


    public String getStatus() {
         return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPeerCount() {
        return peerCount;
    }

    public void setPeerCount(int peerCount) {
        this.peerCount = peerCount;
    }

    public long getLatestBlock() {
        return latestBlock;
    }

    public void setLatestBlock(long latestBlock) {
        this.latestBlock = latestBlock;
    }

    public long getPendingTxn() {
        return pendingTxn;
    }

    public void setPendingTxn(long pendingTxn) {
        this.pendingTxn = pendingTxn;
    }

    public boolean isMining() {
        return mining;
    }

    public void setMining(boolean mining) {
        this.mining = mining;
    }

     public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeIP(String ip) {
        this.nodeIP = ip;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public NodeConfig getConfig() {
        return config;
    }

    public void setConfig(NodeConfig config) {
        this.config = config;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }

    public String getRpcUrl() {
        return rpcUrl;
    }

    public void setRpcUrl(String rpcUrl) {
        this.rpcUrl = rpcUrl;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public QuorumInfo getQuorumInfo() {
        return quorumInfo;
    }

    public void setQuorumInfo(QuorumInfo quorumInfo) {
        this.quorumInfo = quorumInfo;
    }

    @Override
    public String toString()  {
        return StringUtils.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
