package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.error.ABIException;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.error.CompilerException;
import com.jpmorgan.cakeshop.model.APIError;
import com.jpmorgan.cakeshop.model.APIResponse;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Controller
public class BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse> handleException(Exception ex) {

        if (!(ex instanceof CompilerException)) {
            if ((ex instanceof IOException) && ex.getMessage().contains("Broken pipe")) {
                LOG.warn(ExceptionUtils.getMessage(ex));
            } else {
                LOG.warn("Caught exception: " + ex.getMessage(), ex);
            }
        }

        APIResponse res = new APIResponse();
        String rootCause = ExceptionUtils.getRootCauseMessage(ex);

        if (ex instanceof CompilerException) {
            List<String> errors = ((CompilerException) ex).getErrors();
            for (String e : errors) {
                APIError err = new APIError(null, HttpStatus.BAD_REQUEST.toString(), "compilation failed");
                err.setDetail(e);
                res.addError(err);
            }
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);

        } else if (ex instanceof ABIException) {
            res.addError(new APIError(null, HttpStatus.BAD_REQUEST.toString(), "Arguments did not match ABI signature", ex.getMessage(), null));
            return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);

        } else if (ex instanceof APIException) {
            // try to pass back more specific exceptions

            Throwable cause = ex.getCause();
            if (cause instanceof ResourceAccessException) {
                // server down (failed connect)
                res.addError(new APIError(null, HttpStatus.SERVICE_UNAVAILABLE.toString(), "Service unavailable", rootCause));
                return new ResponseEntity<>(res, HttpStatus.SERVICE_UNAVAILABLE);

            } else if (cause instanceof HttpClientErrorException) {
                // server returned 4xx
                HttpClientErrorException err = (HttpClientErrorException) cause;
                res.addError(new APIError(null, err.getStatusCode().toString(), err.getStatusText(), rootCause));
                return new ResponseEntity<>(res, err.getStatusCode());

            } else if (cause instanceof HttpServerErrorException) {
                // server returned 5xx
                HttpServerErrorException err = (HttpServerErrorException) cause;
                res.addError(new APIError(null, err.getStatusCode().toString(), err.getStatusText(), rootCause));
                return new ResponseEntity<>(res, err.getStatusCode());
            }
        }

        res.addError(new APIError(null, HttpStatus.INTERNAL_SERVER_ERROR.toString(), "Internal server error", rootCause));
        return new ResponseEntity<>(res, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
