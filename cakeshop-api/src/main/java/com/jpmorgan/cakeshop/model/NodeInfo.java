package com.jpmorgan.cakeshop.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.jpmorgan.cakeshop.util.StringUtils;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;

@Entity
@Table(name = "NODE_INFO")
public class NodeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

    public String name;

    public String rpcUrl;

    public String transactionManagerUrl;

    @JsonInclude()
    @Transient
    public boolean isSelected = false;

    public NodeInfo() {
    }

    public NodeInfo(String name, String rpcUrl, String transactionManagerUrl) {
        this(null, name, rpcUrl, transactionManagerUrl);
    }

    public NodeInfo(Long id, String name, String rpcUrl, String transactionManagerUrl) {
        this.id = id;
        this.name = name;
        this.rpcUrl = rpcUrl;
        this.transactionManagerUrl = transactionManagerUrl;
    }

    @Override
    public String toString() {
        return StringUtils.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
