package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PermissionsInfo {

    @JsonProperty("fullOrgId")
    private String fullOrgId;

    @JsonProperty("level")
    private int level;

    @JsonProperty("orgId")
    private String orgId;

    @JsonProperty("parentOrgId")
    private String parentOrgId;

    @JsonProperty("status")
    private int status;

    @JsonProperty("subOrgList")
    private List<String> subOrgList;

    @JsonProperty("ultimateParent")
    private String ultimateParent;

    public String getFullOrgId() {
        return fullOrgId;
    }

    public void setFullOrgId(String fullOrgId) {
        this.fullOrgId = fullOrgId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getParentOrgId() {
        return parentOrgId;
    }

    public void setParentOrgId(String parentOrgId) {
        this.parentOrgId = parentOrgId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getSubOrgList() {
        return subOrgList;
    }

    public void setSubOrgList(List<String> subOrgList) {
        this.subOrgList = subOrgList;
    }

    public String getUltimateParent() {
        return ultimateParent;
    }

    public void setUltimateParent(String ultimateParent) {
        this.ultimateParent = ultimateParent;
    }

}
