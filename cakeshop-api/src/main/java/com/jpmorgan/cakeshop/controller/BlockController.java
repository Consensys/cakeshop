package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;
import com.jpmorgan.cakeshop.model.Block;
import com.jpmorgan.cakeshop.model.json.BlockPostJsonRequest;
import com.jpmorgan.cakeshop.service.BlockService;
import com.jpmorgan.cakeshop.util.StringUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/block",
        method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class BlockController extends BaseController {

    @Autowired
    BlockService blockService;

    @ApiImplicitParams({
        @ApiImplicitParam(name = "id", required = false, value = "get block by id", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "hash", required = false, value = "get block by hash if id is missing", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "number", required = false, value = "get block by number if id or hash is missing", dataType = "java.lang.Long", paramType = "body")
        ,
        @ApiImplicitParam(name = "tag", required = false, value = "get block by tag if id, hash and number are missing", dataType = "java.lang.String", paramType = "body")
    })
    @RequestMapping("/get")
    public ResponseEntity<APIResponse> getBlock(@RequestBody BlockPostJsonRequest jsonRequest) throws APIException {

        if (StringUtils.isBlank(jsonRequest.getId()) && StringUtils.isNotBlank(jsonRequest.getHash())) {
            jsonRequest.setId(jsonRequest.getHash());  // backwards compat
        }

        Block block = blockService.get(jsonRequest.getId(), jsonRequest.getNumber(), jsonRequest.getTag());

        APIResponse res = new APIResponse();

        if (block != null) {
            res.setData(block.toAPIData());
            return new ResponseEntity<>(res, HttpStatus.OK);
        }

        APIError err = new APIError();
        err.setStatus("404");
        err.setTitle("Block not found");
        res.addError(err);
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }

}
