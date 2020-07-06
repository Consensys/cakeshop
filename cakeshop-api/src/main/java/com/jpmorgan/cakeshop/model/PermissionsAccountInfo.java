package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PermissionsAccountInfo {

    @JsonProperty("acctId")
    private String acctId;

    @JsonProperty("isOrgAdmin")
    private boolean isOrgAdmin;

    @JsonProperty("orgId")
    private String orgId;

    @JsonProperty("roleId")
    private String roleId;

    @JsonProperty("status")
    private int status;

    public String getAcctId() {
        return acctId;
    }

    public void setAcctId(String acctId) {
        this.acctId = acctId;
    }

    public boolean isOrgAdmin() {
        return isOrgAdmin;
    }

    public void setOrgAdmin(boolean orgAdmin) {
        isOrgAdmin = orgAdmin;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
