package com.jpmorgan.cakeshop.client.model.res;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import io.swagger.annotations.ApiModelProperty;





@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JavaClientCodegen", date = "2016-05-26T13:00:18.987-04:00")
public class APIError   {

    private String id = null;
    private String status = null;
    private String code = null;
    private String title = null;
    private String detail = null;


    /**
     * Error ID, if available
     **/
    public APIError id(String id) {
        this.id = id;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Error ID, if available")
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    /**
     * HTTP status code
     **/
    public APIError status(String status) {
        this.status = status;
        return this;
    }

    @ApiModelProperty(example = "null", value = "HTTP status code")
    @JsonProperty("status")
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    /**
     * API-specific error code, if available
     **/
    public APIError code(String code) {
        this.code = code;
        return this;
    }

    @ApiModelProperty(example = "null", value = "API-specific error code, if available")
    @JsonProperty("code")
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }


    /**
     * Short error description
     **/
    public APIError title(String title) {
        this.title = title;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Short error description")
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * Detailed error information
     **/
    public APIError detail(String detail) {
        this.detail = detail;
        return this;
    }

    @ApiModelProperty(example = "null", value = "Detailed error information")
    @JsonProperty("detail")
    public String getDetail() {
        return detail;
    }
    public void setDetail(String detail) {
        this.detail = detail;
    }



    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        APIError aPIError = (APIError) o;
        return Objects.equals(this.id, aPIError.id) &&
                Objects.equals(this.status, aPIError.status) &&
                Objects.equals(this.code, aPIError.code) &&
                Objects.equals(this.title, aPIError.title) &&
                Objects.equals(this.detail, aPIError.detail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, code, title, detail);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}

