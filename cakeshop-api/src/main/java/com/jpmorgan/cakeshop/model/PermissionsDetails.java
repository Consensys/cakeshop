package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PermissionsDetails {

    public static final String API_DATA_TYPE = "permissions";

    @JsonProperty("_result")
    private List<PermissionsInfo> orgList;

    public List<PermissionsInfo> getOrgList() {
        return orgList;
    }

    public void setOrgList(List<PermissionsInfo> orgList) {
        this.orgList = orgList;
    }

    public APIData toAPIData() {
        APIData data = new APIData();
        data.setType(API_DATA_TYPE);
        data.setAttributes(this);
        return data;
    }
}
