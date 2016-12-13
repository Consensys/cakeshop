package com.jpmorgan.cakeshop.client.model.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@JsonInclude(Include.NON_NULL)
public class APIResponse<T, S> {

    /**
     * Response data. Should be an instance of either {@link APIData} or List&lt;APIData&gt;.
     */
    private T apiData;

    private List<APIError> errors;
    private Map<String, String> meta;

    public APIResponse() {
    }

    public void addError(APIError error) {
        if (getErrors() == null) {
            this.errors = new ArrayList<APIError>();
        }
        getErrors().add(error);
    }

    public T getApiData() {
        return apiData;
    }

    @JsonProperty("data")
    public void setApiData(T data) {
        this.apiData = data;
    }

    public List<APIError> getErrors() {
        return errors;
    }

    public void setErrors(List<APIError> errors) {
        this.errors = errors;
    }

    public Map<String, String> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @SuppressWarnings("unchecked")
    public S getData() {
        if (apiData != null && apiData instanceof APIData<?>) {
            Object data = ((APIData<?>) apiData).getAttributes();
            if (data instanceof SimpleResult) {
                return (S) ((SimpleResult) data).getResult();
            }
            return (S) data;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<S> getDataAsList() {
        if (apiData == null || !(apiData instanceof List<?>)) {
            return null;
        }

        List<S> ret = new ArrayList<>();
        for (APIData<S> d :  (List<APIData<S>>) apiData) {
            ret.add(d.getAttributes());
        }

        return ret;
    }
}
