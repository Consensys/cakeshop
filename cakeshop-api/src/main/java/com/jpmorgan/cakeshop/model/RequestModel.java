package com.jpmorgan.cakeshop.model;

import com.jpmorgan.cakeshop.service.GethHttpService;

/**
 * Geth JSON-RPC request model
 *
 * @author Michael Kazansky
 */
public class RequestModel {

    private String jsonrpc;
    private String method;
    private Object[] params;
    private Long id;

    public RequestModel(){}

    public RequestModel(String method, Object[] params, Long id) {
        this(method, params, GethHttpService.GETH_API_VERSION, id);
    }

    public RequestModel(String method, Object[] params, String jsonrpc, Long id) {
        this.method = method;
        this.params = params;

        this.jsonrpc = jsonrpc;
        this.id = id;
    }

    /**
     * @return the jsonrpc
     */
    public String getJsonrpc() {
        return jsonrpc;
    }

    /**
     * @param jsonrpc the jsonrpc to set
     */
    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @param method the method to set
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * @return the params
     */
    public Object[] getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(Object[] params) {
        this.params = params;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

}
