package com.jpmorgan.cakeshop.model;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.ContractABI.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.quorum.methods.request.PrivateTransaction;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionRequest {

    public static final String BLOCK_LATEST = "latest";

    public static final int DEFAULT_GAS = 8_000_000;

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

	public DefaultBlockParameter toBlockParameter() {
		if (isRead) {
			if (blockNumber == null) {
				return DefaultBlockParameter.valueOf(BLOCK_LATEST);
			} else {
				return new DefaultBlockParameterNumber((Integer) blockNumber);
			}
		} else {
			return DefaultBlockParameter.valueOf("");
		}
	}

	public PrivateTransaction toPrivateTransaction() {
		return new PrivateTransaction(fromAddress, null, BigInteger.valueOf(DEFAULT_GAS),
				contractAddress, null, "0x" + function.encodeAsHex(args), privateFrom, privateFor);
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
