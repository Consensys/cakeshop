package com.jpmorgan.cakeshop.client.model.req;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jpmorgan.cakeshop.client.model.Contract.CodeTypeEnum;

public class ContractCompileCommand {

    private String code;
    private CodeTypeEnum codeType;
    private Boolean optimize;

    public ContractCompileCommand() {
    }

    public ContractCompileCommand code(String code) {
        this.code = code;
        return this;
    }

    public ContractCompileCommand codeType(CodeTypeEnum codeType) {
        this.codeType = codeType;
        return this;
    }

    public ContractCompileCommand optimize(Boolean optimize) {
        this.optimize = optimize;
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("code_type")
    public CodeTypeEnum getCodeType() {
        return codeType;
    }

    public void setCodeType(CodeTypeEnum codeType) {
        this.codeType = codeType;
    }

    public Boolean getOptimize() {
        return optimize;
    }

    public void setOptimize(Boolean optimize) {
        this.optimize = optimize;
    }

}
