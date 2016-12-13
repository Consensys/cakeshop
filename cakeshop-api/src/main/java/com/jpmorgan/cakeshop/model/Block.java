package com.jpmorgan.cakeshop.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(name="BLOCKS", indexes = {@Index(name="block_number_idx", columnList = "block_number")})
public class Block implements Serializable {

    public static final String API_DATA_TYPE = "block";

    @Id
    private String id;

    private String parentId;
    @Column(name = "block_number")
    private BigInteger number;

    private String nonce;
    private String sha3Uncles;

    @Lob
    @Column(length=4096)
    private String logsBloom;
    private String transactionsRoot;
    private String stateRoot;
    private String miner;
    private BigInteger difficulty;
    private BigInteger totalDifficulty;
    private String extraData;
    private BigInteger gasLimit;
    private BigInteger gasUsed;
    @Column(name = "timestamp_val")
    private BigInteger timestamp;

    @ElementCollection
    private List<String> transactions = new ArrayList<>();

    @ElementCollection
    private List<String> uncles = new ArrayList<>();

    /**
     * Block number
     * @return 
     **/
    public BigInteger getNumber() {
        return number;
    }

    public void setNumber(BigInteger number) {
        this.number = number;
    }

    /**
     * id of the block
     * @return 
     **/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * id of the parent block
     * @return 
     **/
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * hash of the generated proof-of-work (if avail)
     * @return 
     **/
    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * SHA3 of the uncles data in the block (32 bytes)
     * @return 
     **/
    public String getSha3Uncles() {
        return sha3Uncles;
    }

    public void setSha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }

    /**
     * the bloom filter for the logs of the block (256 bytes)
     * @return 
     **/
    public String getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }

    /**
     * the root of the transaction trie of the block (32 bytes)
     * @return 
     **/
    public String getTransactionsRoot() {
        return transactionsRoot;
    }

    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }

    /**
     * the root of the final state trie of the block (32 bytes)
     * @return 
     **/
    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    /**
     * the address of the beneficiary to whom the mining rewards were given (20
     * bytes)
     * @return 
     **/
    public String getMiner() {
        return miner;
    }

    public void setMiner(String miner) {
        this.miner = miner;
    }

    /**
     * integer of the difficulty of this block
     * @return 
     **/
    public BigInteger getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(BigInteger difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * integer of the total difficulty of the chain until this block
     * @return 
     **/
    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }

    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }

    /**
     * the \"extra data\" field for this block
     * @return 
     **/
    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    /**
     * the maximum gas allowed in this block
     * @return 
     **/
    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    /**
     * the total gas used by all transactions in this block
     * @return 
     **/
    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    /**
     * the unix timestamp for when the block was collated
     * @return 
     **/
    public BigInteger getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Array of transaction hashes
     * @return 
     **/
    public List<String> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }

    /**
     * Array of uncle hashes
     * @return 
     **/
    public List<String> getUncles() {
        return uncles;
    }

    public void setUncles(List<String> uncles) {
        this.uncles = uncles;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setId(getId());
        data.setType(API_DATA_TYPE);
        data.setAttributes(this);
        return data;
    }

    /**
     * Blocks are considered equal if their IDs (hashes) are the same
     */
    @Override
    public boolean equals(Object obj) {
        Block otherBlock = (Block) obj;
        return this.id.contentEquals(otherBlock.getId());
    }
}
