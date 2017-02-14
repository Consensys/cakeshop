package com.jpmorgan.cakeshop.model;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.ContractABI.Function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TransactionRequest {

    public static final String BLOCK_LATEST = "latest";

    public static final int DEFAULT_GAS = 3_149_000;

    private String fromAddress;

    private String contractAddress;

    private ContractABI abi;

    private Function function;

    private final Object[] args;

    private String privateFrom;

    private List<String> privateFor;

    private Object blockNumber;

    private final boolean isRead;

    public TransactionRequest(String fromAddress, String contractAddress, ContractABI abi, String method, Object[] args, boolean isRead) throws APIException {
        this(fromAddress, contractAddress, abi, method, args, isRead, null);
    }

    public TransactionRequest(String fromAddress, String contractAddress, ContractABI abi, String method, Object[] args, boolean isRead, Object blockNumber) throws APIException {
        this.fromAddress = fromAddress;
        this.contractAddress = contractAddress;
        this.abi = abi;
        this.isRead = isRead;
        this.blockNumber = blockNumber;
        this.args = args;

        this.privateFrom = null;
        this.privateFor = null;

        this.function = abi.getFunction(method);
        if (this.function == null) {
            throw new APIException("Invalid method '" + method + "'");
        }
    }

    public Object[] toGethArgs() {

        Map<String, Object> req = new HashMap<>();
        req.put("from", fromAddress);
        req.put("to", contractAddress);
        req.put("gas", DEFAULT_GAS);
        req.put("data", function.encodeAsHex(args));

        if (StringUtils.isNotBlank(privateFrom)) {
            req.put("privateFrom", privateFrom);
        }

        if (privateFor != null && !privateFor.isEmpty()) {
            req.put("privateFor", privateFor);
        }

        if (isRead) {
            if (blockNumber == null) {
                return new Object[]{req, BLOCK_LATEST};
            } else {
                return new Object[]{req, blockNumber};
            }
        } else {
            return new Object[]{req};
        }
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public ContractABI getAbi() {
        return abi;
    }

    public void setAbi(ContractABI abi) {
        this.abi = abi;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
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
