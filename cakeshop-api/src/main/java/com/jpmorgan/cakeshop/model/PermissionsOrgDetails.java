package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PermissionsOrgDetails {

    public static final String API_DATA_TYPE = "permissions";

    @JsonProperty("nodeList")
    private List<PermissionsNodeInfo> nodeList;

    @JsonProperty("acctList")
    private List<PermissionsAccountInfo> accountList;

    @JsonProperty("roleList")
    private List<PermissionsRoleInfo> roleList;

    @JsonProperty("subOrgList")
    private List<String> subOrgList;

    public List<PermissionsNodeInfo> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<PermissionsNodeInfo> nodeList) {
        this.nodeList = nodeList;
    }

    public List<PermissionsAccountInfo> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<PermissionsAccountInfo> accountList) {
        this.accountList = accountList;
    }

    public List<PermissionsRoleInfo> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<PermissionsRoleInfo> roleList) {
        this.roleList = roleList;
    }

    public List<String> getSubOrgList() {
        return subOrgList;
    }

    public void setSubOrgList(List<String> subOrgList) {
        this.subOrgList = subOrgList;
    }

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setType(API_DATA_TYPE);
        data.setAttributes(this);
        return data;
    }
}
