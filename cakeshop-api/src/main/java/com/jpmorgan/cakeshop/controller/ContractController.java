package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.ContractABI;
import com.jpmorgan.cakeshop.model.ContractABI.Entry.Param;
import com.jpmorgan.cakeshop.model.ContractABI.Function;
import com.jpmorgan.cakeshop.model.SolidityType.Bytes32Type;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionRequest;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.model.json.ContractPostJsonRequest;
import com.jpmorgan.cakeshop.service.ContractService;
import com.jpmorgan.cakeshop.service.ContractService.CodeType;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

@RestController
@RequestMapping(value = "/api/contract",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class ContractController extends BaseController {

    @Autowired
    private ContractService contractService;

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = true, value = "Contract address", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/get")
    public ResponseEntity<APIResponse> getContract(@RequestBody ContractPostJsonRequest jsonRequest) throws APIException {

        Contract contract = contractService.get(jsonRequest.getAddress());

        APIResponse res = new APIResponse();

        if (contract != null) {
            res.setData(toAPIData(contract));
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Contract not found");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", required = false, value = "Required. Contract code", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "code_type", required = false, value = "Required. Only solidity is supported for now.", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "optimize", required = false, value = "Optimize code.", dataType = "java.lang.Boollean", paramType = "body")
    })
    @RequestMapping("/compile")
    public ResponseEntity<APIResponse> compile(@RequestBody ContractPostJsonRequest jsonRequest) throws APIException {

        List<Contract> contracts = contractService.compile(jsonRequest.getCode(),
                CodeType.valueOf(jsonRequest.getCode_type()), jsonRequest.getOptimize());
        APIResponse res = new APIResponse();

        if (contracts != null) {
            res.setData(toAPIData(contracts));
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("400");
        err.setTitle("Bad Request");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "code", required = false, value = "Required. Contract code", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "code_type", required = false, value = "Required. Only solidity is supported for now.", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "from", required = false, value = "Required. Account from address", dataType = "java.lang.String", paramType = "body")
        , 
        @ApiImplicitParam(name = "args", required = false, value = "Required. Function arguments to pass", dataType = "java.util.Arrays", paramType = "body")
        ,
        @ApiImplicitParam(name = "binary", required = false, value = "Bynary contract code.", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "privateFrom", required = false, value = "Private from Account", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "privateFor", required = false, value = "Private for Account", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/create")
    public ResponseEntity<APIResponse> create(@RequestBody ContractPostJsonRequest jsonRequest) throws APIException {

        TransactionResult tx = contractService.create(jsonRequest.getFrom(), jsonRequest.getCode(),
                CodeType.valueOf(jsonRequest.getCode_type()), jsonRequest.getArgs(), jsonRequest.getBinary(),
                jsonRequest.getPrivateFrom(), jsonRequest.getPrivateFor());

        APIResponse res = new APIResponse();

        if (tx != null) {
            res.setData(tx.toAPIData());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("400");
        err.setTitle("Bad Request");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    @RequestMapping("/list")
    public ResponseEntity<APIResponse> list() throws APIException {
        List<Contract> contracts = contractService.list();
        APIResponse res = new APIResponse();
        res.setData(toAPIData(contracts));

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "from", required = false, value = "Required. Account from address", dataType = "java.lang.String", paramType = "body")
        , 
        @ApiImplicitParam(name = "address", required = false, value = "Required. Account address", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "method", required = false, value = "Required. Contract method to execute.", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "args", required = false, value = "Required. Args for contratc method", dataType = "java.util.Arrays", paramType = "body")
    })
    @RequestMapping("/read")
    public ResponseEntity<APIResponse> read(@RequestBody ContractPostJsonRequest jsonRequest) throws APIException {

        Object result = contractService.read(createTransactionRequest(jsonRequest.getFrom(), jsonRequest.getAddress(),
                jsonRequest.getMethod(), jsonRequest.getArgs(), true, jsonRequest.getBlockNumber()));
        APIResponse res = new APIResponse();
        res.setData(result);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private TransactionRequest createTransactionRequest(String from, String address, String method, Object[] args, boolean isRead, Object blockNumber) throws APIException {
        ContractABI abi = contractService.get(address).getContractAbi();
        if (abi == null) {
            throw new APIException("Contract adddress " + address + " is not in the registry");
        }

        Function func = abi.getFunction(method);
        if (func == null) {
            throw new APIException("Method '" + method + "' does not exist in contract at " + address);
        }

        args = decodeArgs(func, args);

        return new TransactionRequest(from, address, abi, method, args, isRead);
    }

    /**
     * Handle Base64 encoded byte/string inputs (byte arrays must be base64
     * encoded to put them on the wire w/ JSON)
     *
     * @param method
     * @param args
     * @return
     * @throws APIException
     */
    private Object[] decodeArgs(Function method, Object[] args) throws APIException {
        if (args == null || args.length == 0) {
            return args;
        }

        List<Param> params = method.inputs;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            Param param = params.get(i);
            if (param.type instanceof Bytes32Type && arg instanceof String) {
                args[i] = new String(Base64.decode((String) arg));
            }
        }

        return args;
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "from", required = false, value = "Required. Account from address", dataType = "java.lang.String", paramType = "body")
        , 
        @ApiImplicitParam(name = "address", required = false, value = "Required. Account address", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "method", required = false, value = "Required. Contract method to execute.", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "args", required = false, value = "Required. Args for contract method", dataType = "java.util.Arrays", paramType = "body")
        ,
        @ApiImplicitParam(name = "privateFrom", required = false, value = "Account private from", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "privateFor", required = false, value = "Account private for", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/transact")
    public WebAsyncTask<ResponseEntity<APIResponse>> transact(@RequestBody ContractPostJsonRequest jsonRequest) throws APIException {

        Callable<ResponseEntity<APIResponse>> callable = () -> {
            TransactionRequest req = createTransactionRequest(jsonRequest.getFrom(), jsonRequest.getAddress(),
                    jsonRequest.getMethod(), jsonRequest.getArgs(), false, null);
            req.setPrivateFrom(jsonRequest.getPrivateFrom());
            req.setPrivateFor(jsonRequest.getPrivateFor());

            TransactionResult tr = contractService.transact(req);
            APIResponse res = new APIResponse();
            res.setData(tr.toAPIData());
            ResponseEntity<APIResponse> response = new ResponseEntity<>(res, HttpStatus.OK);
            return response;
        };
        WebAsyncTask asyncTask = new WebAsyncTask(callable);
        return asyncTask;
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "address", required = false, value = "Required. Contract Address", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/transactions/list")
    public ResponseEntity<APIResponse> listTransactions(@RequestBody final ContractPostJsonRequest jsonRequest) throws APIException {

        List<Transaction> txns = contractService.listTransactions(jsonRequest.getAddress());

        List<APIData> data = new ArrayList<>();
        txns.forEach((tx) -> {
            data.add(tx.toAPIData());
        });

        APIResponse res = new APIResponse();
        res.setData(data);

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    private APIData toAPIData(Contract c) {
        return new APIData(c.getAddress(), Contract.API_DATA_TYPE, c);
    }

    private List<APIData> toAPIData(List<Contract> contracts) {
        List<APIData> data = new ArrayList<>();
        contracts.forEach((c) -> {
            data.add(toAPIData(c));
        });
        return data;
    }

}
