package com.jpmorgan.cakeshop.model;

import com.jpmorgan.cakeshop.error.APIException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.quorum.methods.request.PrivateTransaction;

public class DirectTransactionRequest {

    public static final String BLOCK_LATEST = "latest";

    public static final int DEFAULT_GAS = 10_000_000;

    private String fromAddress;

    private String toAddress;

    private final String data;

    private String privateFrom;

    private List<String> privateFor;

    private Object blockNumber;

    private final boolean isRead;

    public DirectTransactionRequest(String fromAddress, String toAddress, String data, boolean isRead) throws APIException {
        this(fromAddress, toAddress, data, isRead, null);
    }

    public DirectTransactionRequest(String fromAddress, String toAddress, String data, boolean isRead, Object blockNumber) throws APIException {
        this.fromAddress = fromAddress;
        this.toAddress = toAddress;
        this.isRead = isRead;
        this.blockNumber = blockNumber;
        this.data = data;

        this.privateFrom = null;
        this.privateFor = null;
    }

    public PrivateTransaction toPrivateTransaction() {
    	return new PrivateTransaction(fromAddress, null, BigInteger.valueOf(DEFAULT_GAS), toAddress, null, data, privateFrom, privateFor);
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getContractAddress() {
        return toAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.toAddress = contractAddress;
    }

    public Object getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Object blockNumber) {
        this.blockNumber = blockNumber;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public String getPrivateFrom() {
        return privateFrom;
    }

    public void setPrivateFrom(String privateFrom) {
        this.privateFrom = privateFrom;
    }

    public List<String> getPrivateFor() {
        return privateFor;
    }

    public void setPrivateFor(List<String> privateFor) {
        this.privateFor = privateFor;
    }

}
