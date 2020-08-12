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
import com.jpmorgan.cakeshop.model.Web3DefaultResponseType;
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
import org.springframework.stereotype.Service;
import org.web3j.quorum.Quorum;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private ObjectMapper jsonMapper;

    private BlockScanner blockScanner;

    private boolean connected;

    private String currentRpcUrl;
    private String currentTransactionManagerUrl;

    private StreamLogAdapter stdoutLogger;
    private StreamLogAdapter stderrLogger;

    private final List<ErrorLog> startupErrors;

    private Quorum quorumService;

    private Web3jService cakeshopService;

    public GethHttpServiceImpl() {
        this.startupErrors = new ArrayList<>();
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

    private void resetCakeshopService() {

        cakeshopService = null;
        quorumService = null;
    }

    @Override
    public Request<?, Web3DefaultResponseType> createHttpRequestType(String funcName, Object... args) throws APIException{
    	return new Request<>(funcName, Arrays.asList(args), getCakeshopService(), Web3DefaultResponseType.class);
    }

    @Override
    public Map<String, Object> executeGethCall(String funcName, Object... args) throws APIException {
        LOG.info("Geth call: " + funcName);
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
                resetCakeshopService();
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
