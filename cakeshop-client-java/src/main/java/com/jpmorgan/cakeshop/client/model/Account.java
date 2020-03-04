package com.jpmorgan.cakeshop.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;


public class Account   {

    private String address = null;
    private String balance = null;


    /**
     * 160-bit identifier
     **/
    public Account address(String address) {
        this.address = address;
        return this;
    }

    @ApiModelProperty(example = "null", value = "160-bit identifier")
    @JsonProperty("address")
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }


    /**
     * A scalar value equal to the number of Wei owned by this address.
     **/
    public Account balance(String balance) {
        this.balance = balance;
        return this;
    }

    @ApiModelProperty(example = "null", value = "A scalar value equal to the number of Wei owned by this address.")
    @JsonProperty("balance")
    public String getBalance() {
        return balance;
    }
    public void setBalance(String balance) {
        this.balance = balance;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        return Objects.equals(this.address, account.address) &&
                Objects.equals(this.balance, account.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, balance);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

}

