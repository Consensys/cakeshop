package com.jpmorgan.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class Contract   {

    private String address = null;
    private String author = null;
    private List<String> keys = new ArrayList<String>();
    private String abi = null;
    private String code = null;
    private String binary = null;
    private String name = null;
    private String owner = null;

    private Long createdDate;
    private Map<String, Object> gasEstimates;
    private String solidityInterface;
    private Map<String, String> functionHashes;


    public enum CodeTypeEnum {
        SOLIDITY("solidity");

        private final String value;

        CodeTypeEnum(String value) {
            this.value = value;
        }

        @Override
        @JsonValue
        public String toString() {
            return value;
        }
    }

    private CodeTypeEnum codeType = CodeTypeEnum.SOLIDITY;


    /**
     * Contract unique identifier. In Ethereum this is the contract address.
     **/
    public Contract address(String address) {
        this.address = address;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Contract unique identifier. In Ethereum this is the contract address.")
    @JsonProperty("address")
    public String getAddress() {
        return address;
    }
    public void setAddress(String id) {
        this.address = id;
    }


    /**
     * Pubkey of sender on original create.
     **/
    public Contract author(String author) {
        this.author = author;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Pubkey of sender on original create.")
    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }


    /**
     * Pubkeys that can access contract.
     **/
    public Contract keys(List<String> keys) {
        this.keys = keys;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Pubkeys that can access contract.")
    @JsonProperty("keys")
    public List<String> getKeys() {
        return keys;
    }
    public void setKeys(List<String> keys) {
        this.keys = keys;
    }


    /**
     * Contract ABI
     **/
    public Contract abi(String abi) {
        this.abi = abi;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Contract ABI")
    @JsonProperty("abi")
    public String getAbi() {
        return abi;
    }
    public void setAbi(String abi) {
        this.abi = abi;
    }


    /**
     * Contract source code
     **/
    public Contract code(String code) {
        this.code = code;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Contract source code")
    @JsonProperty("code")
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }


    /**
     * Type of code being submitted
     **/
    public Contract codeType(CodeTypeEnum codeType) {
        this.codeType = codeType;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Type of code being submitted")
    @JsonProperty("codeType")
    public CodeTypeEnum getCodeType() {
        return codeType;
    }
    public void setCodeType(CodeTypeEnum codeType) {
        this.codeType = codeType;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contract contract = (Contract) o;
        return Objects.equals(this.address, contract.address) &&
                Objects.equals(this.author, contract.author) &&
                Objects.equals(this.keys, contract.keys) &&
                Objects.equals(this.abi, contract.abi) &&
                Objects.equals(this.code, contract.code) &&
                Objects.equals(this.codeType, contract.codeType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, author, keys, abi, code, codeType);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public Contract name(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Contract owner(String owner) {
        this.owner = owner;
        return this;
    }
    @JsonProperty("owner")
    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Contract binary(String binary) {
        this.binary = binary;
        return this;
    }
    @JsonProperty("binary")
    public String getBinary() {
        return binary;
    }
    public void setBinary(String binary) {
        this.binary = binary;
    }

    public Contract createdDate(Long createdDate) {
        this.createdDate = createdDate;
        return this;
    }
    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }

    public Map<String, Object> getGasEstimates() {
        return gasEstimates;
    }

    public void setGasEstimates(Map<String, Object> gasEstimates) {
        this.gasEstimates = gasEstimates;
    }

    public String getSolidityInterface() {
        return solidityInterface;
    }

    public void setSolidityInterface(String solidityInterface) {
        this.solidityInterface = solidityInterface;
    }

    public Map<String, String> getFunctionHashes() {
        return functionHashes;
    }

    public void setFunctionHashes(Map<String, String> functionHashes) {
        this.functionHashes = functionHashes;
    }

}

