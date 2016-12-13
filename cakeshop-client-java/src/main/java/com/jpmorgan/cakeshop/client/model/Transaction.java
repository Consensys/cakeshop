package com.jpmorgan.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T17:07:52.216-04:00")
public class Transaction   {

    private String id = null;
    private String status = null;
    private String nonce = null;
    private String blockId = null;
    private Long blockNumber = null;
    private Integer transactionIndex = null;
    private String from = null;
    private String to = null;
    private Object input = null;
    private Object decodedInput = null;
    private Long value = null;
    private Long gas = null;
    private Long gasPrice = null;
    private Long cumulativeGasUsed = null;
    private Long gasUsed = null;
    private String contractAddress = null;
    private List<Event> logs = new ArrayList<>();

    private String r;
    private String s;
    private String v;


    /**
     **/
    public Transaction id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     **/
    public Transaction status(String status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    /**
     **/
    public Transaction nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("nonce")
    public String getNonce() {
        return nonce;
    }
    public void setNonce(String nonce) {
        this.nonce = nonce;
    }


    /**
     **/
    public Transaction blockId(String blockId) {
        this.blockId = blockId;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("blockId")
    public String getBlockId() {
        return blockId;
    }
    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }


    /**
     **/
    public Transaction blockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("blockNumber")
    public Long getBlockNumber() {
        return blockNumber;
    }
    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }


    /**
     * integer of the transaction's index position in the block
     **/
    public Transaction transactionIndex(Integer transactionIndex) {
        this.transactionIndex = transactionIndex;
        return this;
    }

    @ApiModelProperty(example = "null", value = "integer of the transaction's index position in the block")
    @JsonProperty("transactionIndex")
    public Integer getTransactionIndex() {
        return transactionIndex;
    }
    public void setTransactionIndex(Integer transactionIndex) {
        this.transactionIndex = transactionIndex;
    }


    /**
     * address of the sender
     **/
    public Transaction from(String from) {
        this.from = from;
        return this;
    }

    @ApiModelProperty(example = "null", value = "address of the sender")
    @JsonProperty("from")
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }


    /**
     * address of the receiver. null when it is a contract creation transaction.
     **/
    public Transaction to(String to) {
        this.to = to;
        return this;
    }

    @ApiModelProperty(example = "null", value = "address of the receiver. null when it is a contract creation transaction.")
    @JsonProperty("to")
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }


    /**
     **/
    public Transaction input(Object input) {
        this.input = input;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("input")
    public Object getInput() {
        return input;
    }
    public void setInput(Object input) {
        this.input = input;
    }


    /**
     **/
    public Transaction decodedInput(Object decodedInput) {
        this.decodedInput = decodedInput;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("decodedInput")
    public Object getDecodedInput() {
        return decodedInput;
    }
    public void setDecodedInput(Object decodedInput) {
        this.decodedInput = decodedInput;
    }


    /**
     **/
    public Transaction value(Long value) {
        this.value = value;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("value")
    public Long getValue() {
        return value;
    }
    public void setValue(Long value) {
        this.value = value;
    }


    /**
     **/
    public Transaction gas(Long gas) {
        this.gas = gas;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("gas")
    public Long getGas() {
        return gas;
    }
    public void setGas(Long gas) {
        this.gas = gas;
    }


    /**
     **/
    public Transaction gasPrice(Long gasPrice) {
        this.gasPrice = gasPrice;
        return this;
    }

    @ApiModelProperty(example = "null", value = "")
    @JsonProperty("gasPrice")
    public Long getGasPrice() {
        return gasPrice;
    }
    public void setGasPrice(Long gasPrice) {
        this.gasPrice = gasPrice;
    }


    /**
     * the total amount of gas used when this transaction was executed in the block.
     **/
    public Transaction cumulativeGasUsed(Long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the total amount of gas used when this transaction was executed in the block.")
    @JsonProperty("cumulativeGasUsed")
    public Long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }
    public void setCumulativeGasUsed(Long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }


    /**
     * the amount of gas used by this specific transaction alone.
     **/
    public Transaction gasUsed(Long gasUsed) {
        this.gasUsed = gasUsed;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the amount of gas used by this specific transaction alone.")
    @JsonProperty("gasUsed")
    public Long getGasUsed() {
        return gasUsed;
    }
    public void setGasUsed(Long gasUsed) {
        this.gasUsed = gasUsed;
    }


    /**
     * the contract address created, if this transaction was a contract creation, otherwise null.
     **/
    public Transaction contractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
        return this;
    }

    @ApiModelProperty(example = "null", value = "the contract address created, if this transaction was a contract creation, otherwise null.")
    @JsonProperty("contractAddress")
    public String getContractAddress() {
        return contractAddress;
    }
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }


    /**
     * List of Events
     **/
    public Transaction logs(List<Event> logs) {
        this.logs = logs;
        return this;
    }

    @ApiModelProperty(example = "null", value = "List of Events")
    @JsonProperty("logs")
    public List<Event> getLogs() {
        return logs;
    }
    public void setLogs(List<Event> logs) {
        this.logs = logs;
    }

    @JsonProperty("r")
    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }

    @JsonProperty("s")
    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    @JsonProperty("v")
    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transaction transaction = (Transaction) o;
        return Objects.equals(this.id, transaction.id) &&
                Objects.equals(this.status, transaction.status) &&
                Objects.equals(this.nonce, transaction.nonce) &&
                Objects.equals(this.blockId, transaction.blockId) &&
                Objects.equals(this.blockNumber, transaction.blockNumber) &&
                Objects.equals(this.transactionIndex, transaction.transactionIndex) &&
                Objects.equals(this.from, transaction.from) &&
                Objects.equals(this.to, transaction.to) &&
                Objects.equals(this.input, transaction.input) &&
                Objects.equals(this.decodedInput, transaction.decodedInput) &&
                Objects.equals(this.value, transaction.value) &&
                Objects.equals(this.gas, transaction.gas) &&
                Objects.equals(this.gasPrice, transaction.gasPrice) &&
                Objects.equals(this.cumulativeGasUsed, transaction.cumulativeGasUsed) &&
                Objects.equals(this.gasUsed, transaction.gasUsed) &&
                Objects.equals(this.contractAddress, transaction.contractAddress) &&
                Objects.equals(this.logs, transaction.logs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, nonce, blockId, blockNumber, transactionIndex, from, to, input, decodedInput, value, gas, gasPrice, cumulativeGasUsed, gasUsed, contractAddress, logs);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}

