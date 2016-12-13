package com.jpmorgan.cakeshop.model;

import com.jpmorgan.cakeshop.db.JpaJsonConverter;
import com.jpmorgan.cakeshop.util.StringUtils;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="EVENTS")
public class Event implements Serializable {

    public static final String API_DATA_TYPE = "event";

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private BigInteger id;

    private String blockId;
    private BigInteger blockNumber;

    private BigInteger logIndex;
    private String transactionId;
    private String contractId;

    private String name;

    //@Lob // does not work, not sure why. hibernate issue.
    @Basic
    @Column(name = "event_data", length = Integer.MAX_VALUE)
    @Convert(converter = JpaJsonConverter.class)    
    private Object[] data;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public BigInteger getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(BigInteger blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BigInteger getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(BigInteger logIndex) {
        this.logIndex = logIndex;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return StringUtils.toString(this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
