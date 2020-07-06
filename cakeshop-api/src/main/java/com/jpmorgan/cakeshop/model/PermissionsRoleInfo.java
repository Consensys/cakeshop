package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PermissionsRoleInfo {

    @JsonProperty("access")
    private int access;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("isAdmin")
    private boolean isAdmin;

    @JsonProperty("isVoter")
    private boolean isVoter;

    @JsonProperty("orgId")
    private String orgId;

    @JsonProperty("roleId")
    private String roleId;

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public boolean isVoter() {
        return isVoter;
    }

    public void setVoter(boolean voter) {
        isVoter = voter;
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
}
