package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.service.WalletService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
            for (Account a : accounts) {
               data.add(new APIData(a.getAddress(), "wallet", a));
            }
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

}
