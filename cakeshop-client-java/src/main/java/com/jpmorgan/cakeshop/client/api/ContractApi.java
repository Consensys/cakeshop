package com.jpmorgan.cakeshop.client.api;

import com.jpmorgan.cakeshop.client.ApiClient;
import com.jpmorgan.cakeshop.client.model.Contract;
import com.jpmorgan.cakeshop.client.model.Transaction;
import com.jpmorgan.cakeshop.client.model.TransactionResult;
import com.jpmorgan.cakeshop.client.model.req.ContractCompileCommand;
import com.jpmorgan.cakeshop.client.model.req.ContractCreateCommand;
import com.jpmorgan.cakeshop.client.model.req.ContractMethodCallCommand;
import com.jpmorgan.cakeshop.client.model.res.APIData;
import com.jpmorgan.cakeshop.client.model.res.APIResponse;

import java.util.List;

import javax.inject.Named;

import feign.Headers;
import feign.RequestLine;

public interface ContractApi extends ApiClient.Api {

    @RequestLine("POST /contract/get")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<APIData<Contract>, Contract> get(@Named("address") String address);

    @RequestLine("POST /contract/list")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<List<APIData<Contract>>, Contract> list();

    @RequestLine("POST /contract/compile")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<List<APIData<Contract>>, Contract> compile(ContractCompileCommand command);

    @RequestLine("POST /contract/create")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<APIData<TransactionResult>, TransactionResult> create(ContractCreateCommand command);

    @RequestLine("POST /contract/read")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<List<Object>, Object> read(ContractMethodCallCommand call);

    @RequestLine("POST /contract/transact")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<APIData<TransactionResult>, TransactionResult> transact(ContractMethodCallCommand call);

    @RequestLine("POST /contract/transactions/list")
    @Headers({ "Content-type: application/json", "Accepts: application/json" })
    APIResponse<List<APIData<Transaction>>, Transaction> listTransactions(@Named("address") String address);

}
