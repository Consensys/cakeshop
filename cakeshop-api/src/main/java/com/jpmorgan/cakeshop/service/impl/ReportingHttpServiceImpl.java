package com.jpmorgan.cakeshop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Contract;
import com.jpmorgan.cakeshop.model.RequestModel;
import com.jpmorgan.cakeshop.service.ReportingHttpService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * @author Michael Kazansky
 */
@Service
public class ReportingHttpServiceImpl implements ReportingHttpService {

    public static final String SIMPLE_RESULT = "_result";

    private static final Logger LOG = LoggerFactory.getLogger(ReportingHttpServiceImpl.class);

    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${cakeshop.reporting.rpc:}")
    String reportingRpcUrl;

    @Value("${cakeshop.reporting.ui:}")
    String reportingUiUrl;

    public ReportingHttpServiceImpl() {
    }

    @Override
    public String getReportingUrl() {
        return reportingRpcUrl;
    }

    @Override
    public String getReportingUiUrl() {
        return reportingUiUrl;
    }

    @Override
    public void registerContract(Contract contract) throws APIException {
        Map<String, String> body = new HashMap<>();
        // SimpleStorage@0xab834cd will be the template name in the reporting tool
        String name = String.format("%s@%s", contract.getName(), contract.getAddress().substring(0, 8));
        body.put("name", name);
        body.put("abi", contract.getABI());
        body.put("storageLayout", contract.getStorageLayout());
        executeReportingCall("reporting.AddTemplate", body);

        body = new HashMap<>();
        body.put("address", contract.getAddress());
        executeReportingCall("reporting.AddAddress", body);

        body = new HashMap<>();
        body.put("address", contract.getAddress());
        body.put("data", name);
        executeReportingCall("reporting.AssignTemplate", body);
    }

    @Override
    public List<String> getRegisteredAddresses() throws APIException {
        Map<String, Object> result = executeReportingCall("reporting.GetAddresses");
        return (List<String>) result.get(SIMPLE_RESULT);
    }

    @Override
    public Map<String, Object> executeReportingCall(String funcName, Object... args) throws APIException {
        LOG.info("Reporting tool call: " + funcName);
        String response;
        try {
            if (StringUtils.isEmpty(reportingRpcUrl)) {
                throw new ResourceAccessException("Current Reporting URL not set, skipping request");
            }

            HttpHeaders jsonContentHeaders = new HttpHeaders();
            jsonContentHeaders.setContentType(APPLICATION_JSON);

            String request;
            try {
                request = jsonMapper.writeValueAsString(new RequestModel(funcName, args, "2.0", 100L));
            } catch (JsonProcessingException e) {
                throw new APIException("Failed to serialize request(s)", e);
            }
            HttpEntity<String> httpEntity = new HttpEntity<>(request, jsonContentHeaders);

            response = restTemplate.exchange(reportingRpcUrl, POST, httpEntity, String.class).getBody();

        } catch (RestClientException e1) {
            LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e1));
            throw new APIException("RPC call failed", e1);
        }

        if (StringUtils.isEmpty(response)) {
            throw new APIException("Received empty reply from server");
        }

        try {
            Map<String, Object> data = jsonMapper.readValue(response, Map.class);

            if (data.containsKey("error") && data.get("error") != null) {
                String message;
                Map<String, String> error = (Map<String, String>) data.get("error");
                if (error.containsKey("message")) {
                    message = error.get("message");
                } else {
                    message = "RPC call failed";
                }
                throw new APIException("RPC request failed: " + message);
            }

            Object result = data.get("result");
            if (result == null) {
                return null;
            }

            if (!(result instanceof Map)) {
                // Handle case where a simple value is returned instead of a map (int, bool, or string)
                Map<String, Object> res = new HashMap<>();
                res.put(SIMPLE_RESULT, data.get("result"));
                return res;
            }

            return (Map<String, Object>) data.get("result");
        } catch (APIException e) {
            throw e;
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }
    }
}
