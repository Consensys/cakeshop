package com.jpmorgan.cakeshop.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.jpmorgan.cakeshop.util.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.*;

@Entity
@Table(name = "NODE_INFO")
public class NodeInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    public Long id;

    public String name;

    public String rpcUrl;

    public String transactionManagerUrl;

    public String reportingUrl;

    @JsonInclude()
    @Transient
    public boolean isSelected = false;

    public NodeInfo() {
    }

    public NodeInfo(String name, String rpcUrl, String transactionManagerUrl, String reportingUrl) {
        this(null, name, rpcUrl, transactionManagerUrl, reportingUrl);
    }

    public NodeInfo(Long id, String name, String rpcUrl, String transactionManagerUrl, String reportingUrl) {
        this.id = id;
        this.name = name;
        this.rpcUrl = rpcUrl;
        this.transactionManagerUrl = transactionManagerUrl;
        this.reportingUrl = reportingUrl;
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
