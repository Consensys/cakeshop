package com.jpmorgan.cakeshop.client.api;

import javax.inject.Named;

import feign.Headers;
import feign.RequestLine;

import com.jpmorgan.cakeshop.client.ApiClient;
import com.jpmorgan.cakeshop.client.model.Transaction;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

import java.util.List;

public interface TransactionApi extends ApiClient.Api {

    /**
     * Retrieve a transaction by id
     *
     * @param id
     *
     * @return APIResponse<APIData<Transaction>>
     */
    @RequestLine("POST /transaction/get")
    @Headers({ "Content-type: application/json", "Accepts: application/json", })
    APIResponse<APIData<Transaction>, Transaction> get(@Named("id") String id);
    
    @RequestLine("POST /transaction/list")
    @Headers({ "Content-type: application/json", "Accepts: application/json", })
    APIResponse<List<APIData<Transaction>>, Transaction> get(@Named("ids") List<String> ids);

}
