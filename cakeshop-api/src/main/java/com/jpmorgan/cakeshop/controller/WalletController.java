package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.json.WalletPostJsonRequest;
import com.jpmorgan.cakeshop.service.WalletService;

import java.util.ArrayList;
import java.util.List;

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

    @RequestMapping("/unlock")
    public ResponseEntity<APIResponse> unlock(@RequestBody WalletPostJsonRequest request) throws APIException {
        Boolean unlocked = walletService.unlockAccount(request);
        APIResponse res = APIResponse.newSimpleResponse(unlocked);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping("/lock")
    public ResponseEntity<APIResponse> lock(@RequestBody WalletPostJsonRequest request) throws APIException {
        Boolean locked = walletService.lockAccount(request);
        APIResponse res = APIResponse.newSimpleResponse(locked);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping("/fund")
    public ResponseEntity<APIResponse> fundAccount(@RequestBody WalletPostJsonRequest request) throws APIException {
        Boolean funded = walletService.fundAccount(request);
        APIResponse res = APIResponse.newSimpleResponse(funded);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

}
