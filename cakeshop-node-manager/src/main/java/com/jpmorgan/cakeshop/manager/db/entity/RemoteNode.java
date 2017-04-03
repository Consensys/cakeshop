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

    public Boolean isClustered() {
        return clustered;
    }

    public void setIsClustered(Boolean isClustered) {
        this.clustered = isClustered;
    }

}
