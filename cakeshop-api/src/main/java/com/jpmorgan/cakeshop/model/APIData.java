package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class APIData {

    private String id;
    private String type;

    private Object attributes;

    public APIData() {
    }

    public APIData(String id, String type, Object attributes) {
        this.id = id;
        this.type = type;
        this.attributes = attributes;
    }

    public APIData id(String id) {
        this.id = id;
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public APIData type(String type) {
        this.type = type;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public APIData attributes(Object attributes) {
        this.attributes = attributes;
        return this;
    }

    public Object getAttributes() {
        return attributes;
    }

    public void setAttributes(Object attributes) {
        this.attributes = attributes;
    }
}
