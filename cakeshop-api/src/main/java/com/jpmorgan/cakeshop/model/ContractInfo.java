package com.jpmorgan.cakeshop.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "CONTRACTS")
public class ContractInfo {

    @Id
    public String address;

    /**
     * Contract (class) name
     */
    public String name;

    /**
     * Contract owner (address)
     */
    public String owner;

    /**
     * Date and time the contract was created
     */
    public long createdDate;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    public String contractJson;

    public ContractInfo() {
    }

    public ContractInfo(String address, String name, String owner, long createdDate, String contractJson) {
        this.address = address;
        this.name = name;
        this.owner = owner;
        this.createdDate = createdDate;
        this.contractJson = contractJson;
    }
}
