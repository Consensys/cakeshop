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

    @Lob
    @Column(length = Integer.MAX_VALUE)
    public String contractJson;

    public ContractInfo() {
    }

    public ContractInfo(String address, String contractJson) {
        this.address = address;
        this.contractJson = contractJson;
    }
}
