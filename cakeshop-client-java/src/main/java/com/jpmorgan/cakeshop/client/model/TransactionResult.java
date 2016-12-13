package com.jpmorgan.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class TransactionResult   {

    private String id = null;


    /**
     * ID of the newly created transaction
     **/
    public TransactionResult id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", required = true, value = "ID of the newly created transaction")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionResult transactionResult = (TransactionResult) o;
        return Objects.equals(this.id, transactionResult.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}

