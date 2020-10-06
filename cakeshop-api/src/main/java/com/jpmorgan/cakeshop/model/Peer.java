package com.jpmorgan.cakeshop.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Table(name="PEERS")
public class Peer implements Serializable  {

    /**
     * Node status
     */
    private String status;

    /**
     * Node ID
     */
    @Id
    private String id;

    /**
     * Node Address
     */
    private String nodeUrl;

    /**
     * Node Name
     */
    private String nodeName;

    /**
     * Node IP
     */
    private String nodeIP;

    /**
     * Raft Id
     */
    private int raftId;

    /**
     * Raft leader
     */
    private boolean leader;

    /**
     * Raft role
     */
    @JsonInclude()
    @Transient
    private String role;

    public String getStatus() {
         return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeIP(String ip) {
        this.nodeIP = ip;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public int getRaftId() {
        return raftId;
    }

    public void setRaftId(int raftId) {
        this.raftId = raftId;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString()  {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
