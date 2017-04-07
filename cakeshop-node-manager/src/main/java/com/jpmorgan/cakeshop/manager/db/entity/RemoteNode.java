package com.jpmorgan.cakeshop.manager.db.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

@Entity
@Table(name = "REMOTE_NODES", indexes = {
    @Index(name = "remote_node_indx", columnList = "id")})
public class RemoteNode {

    @Id
    private String id;
    private String url;
    private String constellationUrl;
    private String nodeAddress;
    private String cred1, cred2;
    private Boolean clustered = false;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getConstellationUrl() {
        return constellationUrl;
    }

    public void setConstellationUrl(String constellationUrl) {
        this.constellationUrl = constellationUrl;
    }

    /**
     * @return the nodeAddress
     */
    public String getNodeAddress() {
        return nodeAddress;
    }

    /**
     * @param nodeAddress the nodeAddress to set
     */
    public void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    /**
     * @return the cred1
     */
    public String getCred1() {
        return cred1;
    }

    /**
     * @param cred1 the cred1 to set
     */
    public void setCred1(String cred1) {
        this.cred1 = cred1;
    }

    /**
     * @return the cred2
     */
    public String getCred2() {
        return cred2;
    }

    /**
     * @param cred2 the cred2 to set
     */
    public void setCred2(String cred2) {
        this.cred2 = cred2;
    }

    public Boolean isClustered() {
        return clustered;
    }

    public void setIsClustered(Boolean isClustered) {
        this.clustered = isClustered;
    }

}
