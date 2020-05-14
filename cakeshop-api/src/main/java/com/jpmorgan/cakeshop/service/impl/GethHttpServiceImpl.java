package com.jpmorgan.cakeshop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.dao.BlockDAO;
import com.jpmorgan.cakeshop.dao.NodeInfoDAO;
import com.jpmorgan.cakeshop.dao.TransactionDAO;
import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.db.BlockScanner;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.error.ErrorLog;
import com.jpmorgan.cakeshop.model.NodeInfo;
import com.jpmorgan.cakeshop.model.RequestModel;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.util.CakeshopUtils;
import com.jpmorgan.cakeshop.util.ProcessUtils;
import com.jpmorgan.cakeshop.util.StreamLogAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 *
 * @author Michael Kazansky
 */
@Service
public class GethHttpServiceImpl implements GethHttpService {

    public static final String SIMPLE_RESULT = "_result";

    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);
    private static final Logger GETH_LOG = LoggerFactory.getLogger("geth");

    @Autowired
    private GethConfig gethConfig;

    @Autowired(required = false)
    private BlockDAO blockDAO;

    @Autowired(required = false)
    private TransactionDAO txDAO;

    @Autowired(required = false)
    private WalletDAO walletDAO;

    @Autowired()
    private NodeInfoDAO nodeInfoDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper jsonMapper;

    private BlockScanner blockScanner;

    private boolean connected;

    private String currentRpcUrl;
    private String currentTransactionManagerUrl;

    private StreamLogAdapter stdoutLogger;
    private StreamLogAdapter stderrLogger;

    private final List<ErrorLog> startupErrors;
    private final HttpHeaders jsonContentHeaders;

    public GethHttpServiceImpl() {
        this.startupErrors = new ArrayList<>();

        this.jsonContentHeaders = new HttpHeaders();
        this.jsonContentHeaders.setContentType(APPLICATION_JSON);
    }

    private String executeGethCallInternal(String json) throws APIException {
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("> " + json);
            }

            if (StringUtils.isEmpty(currentRpcUrl)) {
                throw new ResourceAccessException("Current RPC URL not set, skipping request");
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(json, jsonContentHeaders);
            ResponseEntity<String> response = restTemplate.exchange(currentRpcUrl, POST, httpEntity, String.class);

            String res = response.getBody();

            if (LOG.isDebugEnabled()) {
                LOG.debug("< " + res.trim());
            }

            return res;

        } catch (RestClientException e) {
            LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e));
            throw new APIException("RPC call failed", e);
        }
    }

    private String requestToJson(Object request) throws APIException {
        try {
            return jsonMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new APIException("Failed to serialize request(s)", e);
        }
    }

    @Override
    public Map<String, Object> executeGethCall(String funcName, Object... args) throws APIException {
        LOG.info("Geth call: " + funcName);
        return executeGethCall(new RequestModel(funcName, args, GETH_API_VERSION, GETH_REQUEST_ID));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> executeGethCall(RequestModel request) throws APIException {
        String response = executeGethCallInternal(requestToJson(request));

        if (StringUtils.isEmpty(response)) {
            throw new APIException("Received empty reply from server");
        }

        try {
            return processResponse(jsonMapper.readValue(response, Map.class));
        } catch (APIException e) {
            throw e;
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> batchExecuteGethCall(List<RequestModel> requests) throws APIException {
        String json = requestToJson(requests);
        String response = executeGethCallInternal(json);

        List<Map<String, Object>> responses;
        try {
            responses = jsonMapper.readValue(response, List.class);

            List<Map<String, Object>> results = new ArrayList<>(responses.size());
            for (Map<String, Object> data : responses) {
                results.add(processResponse(data));
            }
            return results;

        } catch (IOException e) {
            throw new APIException("RPC call failed for " + json, e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> processResponse(Map<String, Object> data) throws APIException {

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
    }

    @PreDestroy
    protected void autoStop() {
        // stop solc server
        LOG.info("Stopping solc daemon");
        List<String> args = Lists.newArrayList(
                gethConfig.getNodeJsBinaryName(),
                CakeshopUtils.getSolcPath(),
                "--stop-ipc");

        ProcessBuilder builder = ProcessUtils.createProcessBuilder(args);
        try {
            Process proc = builder.start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public String getCurrentRpcUrl() {
        return currentRpcUrl;
    }

    @Override
    public Boolean isConnected() {
        return this.connected;
    }

    private void setCurrentRpcUrl(String rpcUrl) {
        this.currentRpcUrl = rpcUrl;
    }

    @Override
    public String getCurrentTransactionManagerUrl() {
        return currentTransactionManagerUrl;
    }

    private void setCurrentTransactionManagerUrl(String transactionManagerUrl) {
        this.currentTransactionManagerUrl = transactionManagerUrl;
    }


    @Override
    public void connectToNode(Long nodeId) {
        try {
            NodeInfo node = nodeInfoDao.getById(nodeId);
            if (node != null) {
                setCurrentRpcUrl(node.rpcUrl);
                setCurrentTransactionManagerUrl(node.transactionManagerUrl);
                runPostConnectTasks();
                gethConfig.setSelectedNode(nodeId);
                gethConfig.save();
            } else {
                LOG.info("Node with id {} does not exist", nodeId);
            }
        } catch (IOException e) {
            LOG.error("Could not connect to node with ID: {}", nodeId);
        }
    }

    private void runPostConnectTasks() {
        if(blockScanner != null) {
            LOG.info("Shutting down BlockScanner");
            blockScanner.shutdown();
        }
        // run scanner thread
        LOG.info("Starting new BlockScanner");
        blockScanner = applicationContext.getBean(BlockScanner.class);
        blockScanner.start();
    }

    private Boolean checkConnection() {

        try {
            Map<String, Object> info = executeGethCall("admin_nodeInfo");
            if (info != null && StringUtils.isNotBlank((String) info.get("id"))) {
                return true;
            }
        } catch (APIException e) {
            LOG.debug("geth not yet up: " + e.getMessage());
        }
        return false;
    }

}
