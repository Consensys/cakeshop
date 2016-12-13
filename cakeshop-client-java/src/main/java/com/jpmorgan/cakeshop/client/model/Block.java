package com.jpmorgan.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;
import java.math.BigInteger;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class Block   {

    private String id = null;
    private String parentId = null;
    private Long number = null;
    private String nonce = null;
    private String sha3Uncles = null;
    private String logsBloom = null;
    private String transactionsRoot = null;
    private String stateRoot = null;
    private String miner = null;
    private BigInteger difficulty = null;
    private BigInteger totalDifficulty = null;
    private String extraData = null;
    private Integer gasLimit = null;
    private Integer gasUsed = null;
    private Integer timestamp = null;
    private List<String> transactions = new ArrayList<String>();
    private List<String> uncles = new ArrayList<String>();


    /**
     * Block number
     **/
    public Block number(Long number) {
        this.number = number;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Block number")
    @JsonProperty("number")
    public Long getNumber() {
        return number;
    }
    public void setNumber(Long number) {
        this.number = number;
    }


    /**
     * hash of the block
     **/
    public Block id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "id of the block")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     * hash of the parent block
     **/
    public Block parentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    @ApiModelProperty(example = "null", value = "hash of the parent block")
    @JsonProperty("parentId")
    public String getParentId() {
        return parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }


    /**
     * hash of the generated proof-of-work (if avail)
     **/
    public Block nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    @ApiModelProperty(example = "null", value = "hash of the generated proof-of-work (if avail)")
    @JsonProperty("nonce")
    public String getNonce() {
        return nonce;
    }
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }


    /**
     * SHA3 of the uncles data in the block (32 bytes)
     **/
    public Block sha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
        return this;
    }

    @ApiModelProperty(example = "null", value = "SHA3 of the uncles data in the block (32 bytes)")
    @JsonProperty("sha3Uncles")
    public String getSha3Uncles() {
        return sha3Uncles;
    }
    public void setSha3Uncles(String sha3Uncles) {
        this.sha3Uncles = sha3Uncles;
    }


    /**
     * the bloom filter for the logs of the block (256 bytes)
     **/
    public Block logsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the bloom filter for the logs of the block (256 bytes)")
    @JsonProperty("logsBloom")
    public String getLogsBloom() {
        return logsBloom;
    }
    public void setLogsBloom(String logsBloom) {
        this.logsBloom = logsBloom;
    }


    /**
     * the root of the transaction trie of the block (32 bytes)
     **/
    public Block transactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the root of the transaction trie of the block (32 bytes)")
    @JsonProperty("transactionsRoot")
    public String getTransactionsRoot() {
        return transactionsRoot;
    }
    public void setTransactionsRoot(String transactionsRoot) {
        this.transactionsRoot = transactionsRoot;
    }


    /**
     * the root of the final state trie of the block (32 bytes)
     **/
    public Block stateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the root of the final state trie of the block (32 bytes)")
    @JsonProperty("stateRoot")
    public String getStateRoot() {
        return stateRoot;
    }
    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }


    /**
     * the address of the beneficiary to whom the mining rewards were given (20 bytes)
     **/
    public Block miner(String miner) {
        this.miner = miner;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the address of the beneficiary to whom the mining rewards were given (20 bytes)")
    @JsonProperty("miner")
    public String getMiner() {
        return miner;
    }
    public void setMiner(String miner) {
        this.miner = miner;
    }


    /**
     * integer of the difficulty of this block
     **/
    public Block difficulty(BigInteger difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    @ApiModelProperty(example = "null", value = "integer of the difficulty of this block")
    @JsonProperty("difficulty")
    public BigInteger getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(BigInteger difficulty) {
        this.difficulty = difficulty;
    }


    /**
     * integer of the total difficulty of the chain until this block
     **/
    public Block totalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
        return this;
    }

    @ApiModelProperty(example = "null", value = "integer of the total difficulty of the chain until this block")
    @JsonProperty("totalDifficulty")
    public BigInteger getTotalDifficulty() {
        return totalDifficulty;
    }
    public void setTotalDifficulty(BigInteger totalDifficulty) {
        this.totalDifficulty = totalDifficulty;
    }


    /**
     * the \"extra data\" field for this block
     **/
    public Block extraData(String extraData) {
        this.extraData = extraData;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the \"extra data\" field for this block")
    @JsonProperty("extraData")
    public String getExtraData() {
        return extraData;
    }
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }


    /**
     * the maximum gas allowed in this block
     **/
    public Block gasLimit(Integer gasLimit) {
        this.gasLimit = gasLimit;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the maximum gas allowed in this block")
    @JsonProperty("gasLimit")
    public Integer getGasLimit() {
        return gasLimit;
    }
    public void setGasLimit(Integer gasLimit) {
        this.gasLimit = gasLimit;
    }


    /**
     * the total gas used by all transactions in this block
     **/
    public Block gasUsed(Integer gasUsed) {
        this.gasUsed = gasUsed;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the total gas used by all transactions in this block")
    @JsonProperty("gasUsed")
    public Integer getGasUsed() {
        return gasUsed;
    }
    public void setGasUsed(Integer gasUsed) {
        this.gasUsed = gasUsed;
    }


    /**
     * the unix timestamp for when the block was collated
     **/
    public Block timestamp(Integer timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the unix timestamp for when the block was collated")
    @JsonProperty("timestamp")
    public Integer getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }


    /**
     * Array of transaction hashes
     **/
    public Block transactions(List<String> transactions) {
        this.transactions = transactions;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Array of transaction hashes")
    @JsonProperty("transactions")
    public List<String> getTransactions() {
        return transactions;
    }
    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }


    /**
     * Array of uncle hashes
     **/
    public Block uncles(List<String> uncles) {
        this.uncles = uncles;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Array of uncle hashes")
    @JsonProperty("uncles")
    public List<String> getUncles() {
        return uncles;
    }
    public void setUncles(List<String> uncles) {
        this.uncles = uncles;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Block block = (Block) o;
        return Objects.equals(this.number, block.number) &&
                Objects.equals(this.id, block.id) &&
                Objects.equals(this.parentId, block.parentId) &&
                Objects.equals(this.nonce, block.nonce) &&
                Objects.equals(this.sha3Uncles, block.sha3Uncles) &&
                Objects.equals(this.logsBloom, block.logsBloom) &&
                Objects.equals(this.transactionsRoot, block.transactionsRoot) &&
                Objects.equals(this.stateRoot, block.stateRoot) &&
                Objects.equals(this.miner, block.miner) &&
                Objects.equals(this.difficulty, block.difficulty) &&
                Objects.equals(this.totalDifficulty, block.totalDifficulty) &&
                Objects.equals(this.extraData, block.extraData) &&
                Objects.equals(this.gasLimit, block.gasLimit) &&
                Objects.equals(this.gasUsed, block.gasUsed) &&
                Objects.equals(this.timestamp, block.timestamp) &&
                Objects.equals(this.transactions, block.transactions) &&
                Objects.equals(this.uncles, block.uncles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, id, parentId, nonce, sha3Uncles, logsBloom, transactionsRoot, stateRoot, miner,
                difficulty, totalDifficulty, extraData, gasLimit, gasUsed, timestamp, transactions, uncles);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}
