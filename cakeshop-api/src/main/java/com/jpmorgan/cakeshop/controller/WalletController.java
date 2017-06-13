package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.json.WalletPostJsonRequest;
import com.jpmorgan.cakeshop.service.WalletService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/wallet",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class WalletController extends BaseController {

    @Autowired
    WalletService walletService;

    @RequestMapping("/list")
    public ResponseEntity<APIResponse> getAccounts() throws APIException {

        APIResponse res = new APIResponse();

        List<Account> accounts = walletService.list();
        if (accounts != null) {
            List<APIData> data = new ArrayList<>();
            accounts.forEach((account) -> {
                data.add(new APIData(account.getAddress(), "wallet", account));
            });
            res.setData(data);
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Accounts not found");
        res.addError(err);

        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

    @RequestMapping("/create")
    public ResponseEntity<APIResponse> create() throws APIException {
        Account account = walletService.create();
        APIResponse res = new APIResponse();
        res.setData(new APIData(account.getAddress(), "wallet", account));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "account", required = false, value = "Required. Account to unlock", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "accountPassword", required = false, value = "Password used to create account. Required only if account was created with one.", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/unlock")
    public ResponseEntity<APIResponse> unlock(@RequestBody WalletPostJsonRequest request) throws APIException {
        APIResponse res;
        if (StringUtils.isBlank(request.getAccount())) {
            res = new APIResponse();
            APIError err = new APIError();
            err.setStatus("400");
            err.setTitle("Bad request");
            res.addError(err);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } else {
            res = APIResponse.newSimpleResponse(walletService.unlockAccount(request));
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "account", required = false, value = "Required. Account to lock", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/lock")
    public ResponseEntity<APIResponse> lock(@RequestBody WalletPostJsonRequest request) throws APIException {

        APIResponse res;
        if (StringUtils.isBlank(request.getAccount())) {
            res = new APIResponse();
            APIError err = new APIError();
            err.setStatus("400");
            err.setTitle("Bad request");
            res.addError(err);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } else {
            res = APIResponse.newSimpleResponse(walletService.lockAccount(request));
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
    }

    @ApiImplicitParams({
        @ApiImplicitParam(name = "fromAccount", required = false, value = "Required. Account to fund from", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "account", required = false, value = "Required. Account to fund", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "newBalance", required = false, value = "Required. Fund amount", dataType = "java.lang.Long", paramType = "body")
    })
    @RequestMapping("/fund")
    public ResponseEntity<APIResponse> fundAccount(@RequestBody WalletPostJsonRequest request) throws APIException {

        APIResponse res;
        if (StringUtils.isBlank(request.getFromAccount()) || StringUtils.isBlank(request.getAccount())
                || null == request.getNewBalance()) {
            res = new APIResponse();
            APIError err = new APIError();
            err.setStatus("400");
            err.setTitle("Bad request");
            res.addError(err);
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
        } else {
            res = APIResponse.newSimpleResponse(walletService.fundAccount(request));
            return new ResponseEntity<>(res, HttpStatus.OK);
        }
    }

}
