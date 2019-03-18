package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIData;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Node;
import com.jpmorgan.cakeshop.service.TesseraHttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/tessera", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class TesseraController extends BaseController{

  @Autowired
  TesseraHttpService tesseraHttpService;

  @RequestMapping({"/upcheck"})
  protected ResponseEntity<APIResponse> doGet() throws APIException {

    String response =  tesseraHttpService.getUpdateCheck("http://localhost:9006/upcheck");
    APIResponse apiResponse = new APIResponse();

    return new ResponseEntity<>(APIResponse.newSimpleResponse(response), HttpStatus.OK);
  }
}
