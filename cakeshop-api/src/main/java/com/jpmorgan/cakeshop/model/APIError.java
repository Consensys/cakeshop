package com.jpmorgan.cakeshop.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class APIError {

    private String id;
    private String status; // http status code
    private String code;
    private String title;
    private String detail;
    private String rootCause;

    public APIError() {
    }

    public APIError(String id, String status, String title) {
        this(id, status, title, null, null);
    }

    public APIError(String id, String status, String title, String rootCause) {
        this(id, status, title, rootCause, null);
    }

    public APIError(String id, String status, String title, String detail, String rootCause) {
        this.id = id;
        this.status = status;
        this.title = title;
        this.detail = detail;
        this.rootCause = rootCause;
    }


    public APIError id(String id) {
        this.id = id;
        return this;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public APIError status(String status) {
        this.status = status;
        return this;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public APIError code(String code) {
        this.code = code;
        return this;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public APIError title(String title) {
        this.title = title;
        return this;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public APIError detail(String detail) {
        this.detail = detail;
        return this;
    }
    public String getDetail() {
        return detail;
    }
    public void setDetail(String detail) {
        this.detail = detail;
    }

    public APIError rootCause(String rootCause) {
        this.rootCause = rootCause;
        return this;
    }
    public String getRootCause() {
        return rootCause;
    }
    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }
}
