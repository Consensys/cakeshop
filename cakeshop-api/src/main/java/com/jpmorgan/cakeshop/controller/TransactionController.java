package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.DirectTransactionRequest;
import com.jpmorgan.cakeshop.model.Transaction;
import com.jpmorgan.cakeshop.model.TransactionResult;
import com.jpmorgan.cakeshop.model.json.TransPostJsonResquest;
import com.jpmorgan.cakeshop.service.TransactionService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.WebAsyncTask;

@RestController
@RequestMapping(value = "/api/transaction",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionController extends BaseController {

    @Autowired
    private TransactionService transactionService;

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "Required.Transaction Id", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/get")
    public ResponseEntity<APIResponse> getTransaction(
            @RequestBody TransPostJsonResquest jsonRequest) throws APIException {

        if (StringUtils.isEmpty(jsonRequest.getId())) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                    HttpStatus.BAD_REQUEST);
        }

        Transaction tx = transactionService.get(jsonRequest.getId());

        APIResponse res = new APIResponse();

        if (tx != null) {
            res.setData(tx.toAPIData());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Transaction not found");
        res.addError(err);

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "ids", required = false, value = "Required. Hash or Transaction receipt", dataType = "java.util.List<String>", paramType = "body")
    })
    @RequestMapping("/list")
    public ResponseEntity<APIResponse> getTransactionList(
            @RequestBody TransPostJsonResquest jsonRequest) throws APIException {

        if (null == jsonRequest.getIds() || jsonRequest.getIds().isEmpty()) {
            return new ResponseEntity<>(new APIResponse().error(new APIError().title("Missing param 'id'")),
                    HttpStatus.BAD_REQUEST);
        }
        List<Transaction> txns = transactionService.get(jsonRequest.getIds());
        List<APIData> data = new ArrayList<>();
        APIResponse res = new APIResponse();

        if (txns != null && !txns.isEmpty()) {
            txns.forEach((txn) -> {
                data.add(txn.toAPIData());
            });
            res.setData(data);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Transaction not found");
        res.addError(err);

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "from", required = false, value = "Required. Account from which transaction initiated", dataType = "java.ulang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "to", required = false, value = "Required. Account to which transaction is going to", dataType = "java.ulang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "data", required = false, value = "Required. Transaction data", dataType = "java.ulang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "privateFrom", required = false, value = "Account transaction private from", dataType = "java.ulang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "privateFor", required = false, value = "Account transsaction private for", dataType = "java.ulang.String", paramType = "body")
    })
    @RequestMapping("/save")
    public WebAsyncTask<ResponseEntity<APIResponse>> transact(
            @RequestBody final TransPostJsonResquest jsonRequest) throws APIException {

        Callable<ResponseEntity<APIResponse>> callable = () -> {
            DirectTransactionRequest req = new DirectTransactionRequest(jsonRequest.getFrom(),
                    jsonRequest.getTo(), jsonRequest.getData(), false);
            req.setPrivateFrom(jsonRequest.getPrivateFrom());
            req.setPrivateFor(jsonRequest.getPrivateFor());

            TransactionResult result = transactionService.directTransact(req);
            APIResponse res = new APIResponse();
            res.setData(result.toAPIData());
            ResponseEntity<APIResponse> response = new ResponseEntity<>(res, HttpStatus.OK);
            return response;
        };
        WebAsyncTask asyncTask = new WebAsyncTask(callable);
        return asyncTask;
    }

}
