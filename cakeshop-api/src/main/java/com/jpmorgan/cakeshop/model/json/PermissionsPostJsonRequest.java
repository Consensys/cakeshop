package com.jpmorgan.cakeshop.model.json;

public class PermissionsPostJsonRequest {

    private String id, enodeId, accountId, parentId, roleId;
    private Object f;
    private int action, access;
    private Boolean voter, admin;

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

    public String getEnodeId() {
        return enodeId;
    }

    public void setEnodeId(String enodeId) {
        this.enodeId = enodeId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Object getF() {
        return f;
    }

    public void setF(Object f) {
        this.f = f;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public int getAccess() {
        return access;
    }

    public void setAccess(int access) {
        this.access = access;
    }

    public boolean getVoter() {
        return voter;
    }

    public void setVoter(boolean voter) {
        this.voter = voter;
    }

    public boolean getAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) { this.admin = admin; }
}
