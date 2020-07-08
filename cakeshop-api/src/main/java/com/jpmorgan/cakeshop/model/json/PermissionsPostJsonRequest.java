package com.jpmorgan.cakeshop.model.json;

public class PermissionsPostJsonRequest {

    private String id, enodeId, accountId, parentId, roleId;
    private Object f;
    private int action, access;
    private boolean isVoter, isAdmin;

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

    public boolean isVoter() {
        return isVoter;
    }

    public void setVoter(boolean voter) {
        isVoter = voter;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
