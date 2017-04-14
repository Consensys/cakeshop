package com.jpmorgan.cakeshop.controller;

import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.json.LogViewJsonRequest;
import com.jpmorgan.cakeshop.service.LogViewService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import java.util.Deque;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/api/log", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class LogViewController {

    private final Integer DEFAULT_NUMBER_LINES = 500;

    private final String LOG_PREFIX = System.getProperty("logging.path");

    private final String CONSTELLATION_PATH = com.jpmorgan.cakeshop.util.StringUtils.isNotBlank(System.getProperty("spring.config.location"))
            ? System.getProperty("spring.config.location").replaceAll("file:", "")
                    .replaceAll("application.properties", "").concat("constellation-node/")
            : null;

    @Autowired
    private GethConfigBean gethConfig;

    @Autowired
    private LogViewService service;

    @ApiImplicitParams({
        @ApiImplicitParam(name = "logFileName", required = false, value = "Required. Name of the log to view", dataType = "java.lang.String", paramType = "body")
        , 
        @ApiImplicitParam(name = "logType", required = false, value = "Required. What kind og log", dataType = "java.lang.String", paramType = "body")
        ,
        @ApiImplicitParam(name = "numberLines", required = false, value = "Required. Number of last lines to view", dataType = "java.lang.Integer", paramType = "body")
    })
    @RequestMapping("/view")
    public ResponseEntity getLog(@RequestBody LogViewJsonRequest jsonRequest) throws APIException {

        String logPath = getLogPath(jsonRequest.getLogType(), jsonRequest.getLogFileName());

        if (StringUtils.isNotBlank(logPath)) {
            Deque<String> log = service.getLog(logPath, jsonRequest.getNumberLines() != null ? jsonRequest.getNumberLines() : DEFAULT_NUMBER_LINES);
            return new ResponseEntity(log, OK);
        } else {
            return new ResponseEntity(jsonRequest.getLogType(), BAD_REQUEST);
        }
    }

    private String getLogPath(String logType, String logFileName) {
        String logPath;
        if (StringUtils.isNotBlank(logType)) {
            switch (logType) {
                case "constellation":
                    logPath = StringUtils.isNotBlank(CONSTELLATION_PATH)
                            ? CONSTELLATION_PATH.concat("logs/")
                                    .concat(StringUtils.isNotBlank(logFileName) ? logFileName : "constellation.log")
                            : gethConfig.getDataDirPath().concat("/constellation/logs/")
                                    .concat(StringUtils.isNotBlank(logFileName) ? logFileName : "constellation.log");
                    break;
                case "geth":
                    logPath = LOG_PREFIX.concat("/").concat(StringUtils.isNotBlank(logFileName) ? logFileName : "geth.log");
                    break;
                default:
                    logPath = null;
                    break;
            }

        } else {
            return null;
        }
        return logPath;
    }
}
