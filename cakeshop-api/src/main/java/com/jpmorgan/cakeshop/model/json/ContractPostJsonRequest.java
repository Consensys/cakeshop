/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.model.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@JsonDeserialize(using = ContractDeserializer.class)
public class ContractPostJsonRequest {

    public static final String DEFAULT_CODE_TYPE = "solidity";
    public static final String DEFAULT_EVM_VERSION = "byzantium";

    private String from, code, code_type = DEFAULT_CODE_TYPE, binary, privateFrom, address, method, filename, evmVersion = DEFAULT_EVM_VERSION;
    private Object args[];
    private Object blockNumber;
    private Boolean optimize;
    private List<String> privateFor;

    /**
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the code_type
     */
    public String getCode_type() {
        return code_type;
    }

    /**
     * @param code_type the code_type to set
     */
    public void setCode_type(String code_type) {
        this.code_type = code_type;
    }

    /**
     * @return the binary
     */
    public String getBinary() {
        return binary;
    }

    /**
     * @param binary the binary to set
     */
    public void setBinary(String binary) {
        this.binary = binary;
    }

    /**
     * @return the privateFrom
     */
    public String getPrivateFrom() {
        return privateFrom;
    }

    /**
     * @param privateFrom the privateFrom to set
     */
    public void setPrivateFrom(String privateFrom) {
        this.privateFrom = privateFrom;
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the args
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * @param args the args to set
     */
    public void setArgs(Object[] args) {
        this.args = args;
    }

    /**
     * @return the blockNumber
     */
    public Object getBlockNumber() {
        return blockNumber;
    }

    /**
     * @param blockNumber the blockNumber to set
     */
    public void setBlockNumber(Object blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * @return the optimize
     */
    public Boolean getOptimize() {
        return optimize;
    }

    /**
     * @param optimize the optimize to set
     */
    public void setOptimize(Boolean optimize) {
        this.optimize = optimize;
    }

    /**
     * @param evmVersion version to set
     * e.g. byzantium, constantinople, petersburg
     */
    public void setEvmVersion(String evmVersion) {
        this.evmVersion = evmVersion;
    }

    /**
     * @return the evmVersion
     * e.g. byzantium, constantinople, petersburg
     */
    public String getEvmVersion() {
        return evmVersion;
    }

    /**
     * @return the privateFor
     */
    public List<String> getPrivateFor() {
        return privateFor;
    }

    /**
     * @param privateFor the privateFor to set
     */
    public void setPrivateFor(List<String> privateFor) {
        this.privateFor = privateFor;
    }

}
