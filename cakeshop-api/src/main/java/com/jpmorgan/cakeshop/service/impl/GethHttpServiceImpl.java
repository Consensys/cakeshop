package com.jpmorgan.cakeshop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.dao.BlockDAO;
import com.jpmorgan.cakeshop.dao.NodeInfoDAO;
import com.jpmorgan.cakeshop.dao.TransactionDAO;
import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.db.BlockScanner;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.NodeInfo;
import com.jpmorgan.cakeshop.model.RequestModel;
import com.jpmorgan.cakeshop.model.Web3DefaultResponseType;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.util.CakeshopUtils;
import com.jpmorgan.cakeshop.util.ProcessUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.web3j.quorum.Quorum;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.besu.Besu;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

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
    private ObjectMapper jsonMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${nodejs.binary:node}")
    String nodeJsBinaryName;

    @Value("${cakeshop.reporting.rpc:}")
    String reportingRpcUrl;

    @Value("${cakeshop.reporting.ui:}")
    String reportingUiUrl;

    private BlockScanner blockScanner;

    private boolean connected;

    private String currentRpcUrl;
    private String currentTransactionManagerUrl;

    private Quorum quorumService;

    private Besu besuService;

    private Web3jService cakeshopService;

    public GethHttpServiceImpl() {
    }

    private Web3jService getCakeshopService() throws APIException {
      try {
        if (StringUtils.isEmpty(currentRpcUrl)) {
          throw new ResourceAccessException("Current RPC URL not set, skipping request");
        }
        if (cakeshopService == null) {
          cakeshopService = new HttpService(currentRpcUrl);
          LOG.info("New httpService connected to " + currentRpcUrl);
        }
        return cakeshopService;
      } catch (RestClientException e) {
          LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e));
          throw new APIException("RPC call failed", e);
      }
    }

    public Quorum getQuorumService() throws APIException {
        try {
            if (StringUtils.isEmpty(currentRpcUrl)) {
                throw new ResourceAccessException("Current RPC URL not set, skipping request");
            }
            if (quorumService == null) {
                quorumService = Quorum.build(getCakeshopService());
                LOG.info("New quorum web3j service connected to " + currentRpcUrl);
            }
            return quorumService;
        } catch (RestClientException e) {
            LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e));
            throw new APIException("RPC call failed", e);
        }
    }

    public Besu getBesuService() throws APIException {
        try {
            if (StringUtils.isEmpty(currentRpcUrl)) {
                throw new ResourceAccessException("Current RPC URL not set, skipping request");
            }
            if (besuService == null) {
            	besuService = Besu.build(getCakeshopService());
                LOG.info("New besu web3j service connected to " + currentRpcUrl);
            }
            return besuService;
        } catch (RestClientException e) {
            LOG.error("RPC call failed - " + ExceptionUtils.getRootCauseMessage(e));
            throw new APIException("RPC call failed", e);
        }
    }


    private void resetCakeshopService() {

        cakeshopService = null;
        quorumService = null;
        besuService = null;
    }

    @Override
    public Request<?, Web3DefaultResponseType> createHttpRequestType(String funcName, Object... args) throws APIException{
    	return new Request<>(funcName, Arrays.asList(args), getCakeshopService(), Web3DefaultResponseType.class);
    }

    @Override
    public Map<String, Object> executeGethCall(String funcName, Object... args) throws APIException {
        LOG.debug("Geth call: " + funcName);
        return executeGethCall(createHttpRequestType(funcName, args));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> executeGethCall(Request<?, Web3DefaultResponseType> request) throws APIException {
        try {
            return request.send().getResponse();
        } catch (APIException e) {
            throw e;
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> batchExecuteGethCall(List<Request<?, Web3DefaultResponseType>> requests) throws APIException {
        List<Map<String, Object>> responses = new ArrayList<Map<String,Object>>();
        try {
          for(Request<?, Web3DefaultResponseType> r : requests) {
            responses.add(r.send().getResponse());
          }
          return responses;
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }
    }

    @PreDestroy
    protected void autoStop() {
        // stop solc server
        LOG.info("Stopping solc daemon");
        List<String> args = Lists.newArrayList(
                nodeJsBinaryName,
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

    public String getReportingUrl() {
        return reportingRpcUrl;
    }

    public String getReportingUiUrl() {
        return reportingUiUrl;
    }

    @Override
    public void connectToNode(Long nodeId) {
        try {
            NodeInfo node = nodeInfoDao.getById(nodeId);
            if (node != null) {
                setCurrentRpcUrl(node.rpcUrl);
                setCurrentTransactionManagerUrl(node.transactionManagerUrl);
                resetCakeshopService();
                runPostConnectTasks();
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

    @Override
    public Map<String, Object> executeReportingCall(String funcName, Object... args) throws APIException {
        LOG.info("Reporting tool call: " + funcName);
        String response;
        try {
            if (StringUtils.isEmpty(currentRpcUrl)) {
                throw new ResourceAccessException("Current Reporting URL not set, skipping request");
            }

            HttpHeaders jsonContentHeaders = new HttpHeaders();
            jsonContentHeaders.setContentType(APPLICATION_JSON);

            String request;
            try {
                request = jsonMapper.writeValueAsString(new RequestModel(funcName, args, GETH_API_VERSION, GETH_REQUEST_ID));
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
