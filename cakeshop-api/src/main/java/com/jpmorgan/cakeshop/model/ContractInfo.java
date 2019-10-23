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

    public String contractRegistryAddress;

    @Lob
    @Column(length = Integer.MAX_VALUE)
    public String contractJson;

    public ContractInfo() {
    }

    public ContractInfo(String address, String contractRegistryAddress, String contractJson) {
        this.address = address;
        this.contractRegistryAddress = contractRegistryAddress;
        this.contractJson = contractJson;
    }
}
