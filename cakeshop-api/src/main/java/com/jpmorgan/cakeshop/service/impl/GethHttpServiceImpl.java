package com.jpmorgan.cakeshop.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.bean.GethConfig;
import com.jpmorgan.cakeshop.bean.GethRunner;
import com.jpmorgan.cakeshop.bean.TransactionManager;
import com.jpmorgan.cakeshop.bean.TransactionManagerRunner;
import com.jpmorgan.cakeshop.dao.BlockDAO;
import com.jpmorgan.cakeshop.dao.NodeInfoDAO;
import com.jpmorgan.cakeshop.dao.TransactionDAO;
import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.db.BlockScanner;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.error.ErrorLog;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.NodeInfo;
import com.jpmorgan.cakeshop.model.RequestModel;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.WalletService;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.ProcessUtils;
import com.jpmorgan.cakeshop.util.StreamLogAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.jpmorgan.cakeshop.bean.TransactionManager.Type.TRANSACTION_MANAGER_KEY_NAME;
import static com.jpmorgan.cakeshop.util.ProcessUtils.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 *
 * @author Michael Kazansky
 */
@Service
public class GethHttpServiceImpl implements GethHttpService {

    public static final String SIMPLE_RESULT = "_result";
    public static final Long DEFAULT_NETWORK_ID = 1006L;
    public static final Integer DEFAULT_NUMBER_ACCOUNTS = 3;

    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);
    private static final Logger GETH_LOG = LoggerFactory.getLogger("geth");

    @Autowired
    private GethConfig gethConfig;

    @Autowired
    private GethRunner gethRunner;

    @Autowired
    private TransactionManagerRunner transactionManagerRunner;

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
            LOG.error("RPC request for " + requestToJson(request) + " failed with " + e.getMessage());
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

    @Override
    public Boolean stop() {
        LOG.info("Stopping geth");

        try {
            if (gethRunner.isEmbeddedQuorum()) {
                if (!stopTransactionManager()) {
                    LOG.error("Could not stop constellation");
                }
            }

            if (blockScanner != null) {
                blockScanner.shutdown();
            }

            if (stdoutLogger != null) {
                stdoutLogger.stopAsync();
            }

            if (stderrLogger != null) {
                stdoutLogger.stopAsync();
            }

            return killProcess(gethRunner.getGethPidFilename(), "geth");

        } catch (IOException | InterruptedException ex) {
            LOG.error("Cannot shutdown process " + ex.getMessage());
            return false;
        }
    }

    @CacheEvict(value = "contracts", allEntries = true)
    @Override
    public Boolean reset(String... additionalParams) {

        boolean stopped = this.stop();
        if (!stopped) {
            return stopped;
        }

        try {
            gethRunner.reset();
        } catch (IOException ex) {
            LOG.error("Cannot reset geth data directory", ex);
            return false;
        }


        // delete db
        if (null != blockDAO) {
            blockDAO.reset();
        }
        if (null != txDAO) {
            txDAO.reset();
        }
        if (null != walletDAO) {
            walletDAO.reset();
        }

        return this.start(additionalParams);
    }

    @PreDestroy
    protected void autoStop() {
        if (!gethConfig.isAutoStop()) {
            return;
        }

        stop();

        // stop solc server
        LOG.info("Stopping solc daemon");
        List<String> args = Lists.newArrayList(
                gethConfig.getNodeJsBinaryName(),
                gethRunner.getSolcPath(),
                "--stop-ipc");

        ProcessBuilder builder = ProcessUtils.createProcessBuilder(gethRunner, args);
        try {
            Process proc = builder.start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    @Override
    public void setConnected(boolean connected) {
        this.connected = connected;
        runPostConnectTasks();
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
    public boolean startTransactionManager() {
        return transactionManagerRunner.startTransactionManager();
    }

    @Override
    public Boolean stopTransactionManager() {
        return transactionManagerRunner.stopTransactionManager();
    }

    @Override
    public Boolean start(String... additionalParams) {

        startupErrors.clear();

        if (isProcessRunning(readPidFromFile(gethRunner.getGethPidFilename()))) {
            LOG.info("Ethereum was already running; not starting again");
            return true;
        }

        try {
            String dataDir = gethConfig.getGethDataDirPath();

            gethRunner.downloadQuorumIfNeeded();

            // copy keystore if necessary
            File keystoreDir = new File(FileUtils.expandPath(dataDir, "keystore"));
            if (!keystoreDir.exists()) {
                LOG.debug("Initializing keystore");
                FileUtils.copyDirectory(new File(gethRunner.getKeystorePath()), keystoreDir);
            }

            // run geth init
            File chainDataDir = new File(FileUtils.expandPath(dataDir, "chaindata"));
            File newChainDataDir = new File(FileUtils.expandPath(dataDir, "geth", "chaindata"));
            if (!(chainDataDir.exists() || newChainDataDir.exists())) {
                //chainDataDir.mkdirs();
                LOG.debug("Running consensus mode init");
                gethRunner.initializeConsensusMode();
                LOG.debug("Running geth init");
                if (!initGeth()) {
                    logError("Geth datadir failed to initialize");
                    return false;
                }
            }

            if (gethRunner.isEmbeddedQuorum()) {
                additionalParams = setAdditionalParams(additionalParams).toArray(new String[setAdditionalParams(additionalParams).size()]);
                LOG.info("Embedded quorum, additional params: {}", (Object) additionalParams);

                if (gethConfig.isTransactionManagerEnabled() && !isProcessRunning(
                    readPidFromFile(transactionManagerRunner.getPidFilePath())) && gethConfig
                    .shouldUseQuorum()) {
                    LOG.info("Transaction Manager enabled");
                    startTransactionManager();
                }
            }

            ProcessBuilder builder = createProcessBuilder(gethRunner, createGethCommand(additionalParams));
            final Map<String, String> env = builder.environment();

            if (gethRunner.isEmbeddedQuorum() && gethConfig.shouldUseQuorum()) {
                String transactionManagerIpcPath;
                if (gethConfig.getTransactionManagerType() == TransactionManager.Type.none) {
                    transactionManagerIpcPath = "ignore";
                } else {
                    transactionManagerIpcPath = FileUtils
                        .expandPath(gethConfig.getTransactionManagerDataPath(),
                            TRANSACTION_MANAGER_KEY_NAME + ".ipc");
                    LOG.info("Waiting for tm ipc file to be created: {}", transactionManagerIpcPath);
                    FileUtils.waitFor(new File(transactionManagerIpcPath), 20);
                }
                LOG.info("Setting env variable PRIVATE_CONFIG to: {}", transactionManagerIpcPath);
                env.put("PRIVATE_CONFIG", transactionManagerIpcPath);
            }

            LOG.info("geth command: " +  String.join(" ", builder.command()));
            Process process = builder.start();
            this.stdoutLogger = (StreamLogAdapter) new StreamLogAdapter(GETH_LOG, process.getInputStream()).startAsync();
            this.stderrLogger = (StreamLogAdapter) new StreamLogAdapter(GETH_LOG, process.getErrorStream()).startAsync();

            Integer pid = getProcessPid(process);
            if (pid != null) {
                writePidToFile(pid, gethRunner.getGethPidFilename());
            }

            setEmbeddedNodeAsCurrent();

            if (!(checkGethStarted() && checkWalletUnlocked())) {
                logError("Ethereum failed to start");
                return false;
            }

            setConnected(true);

            // TODO add a watcher thread to make sure it doesn't die..
        } catch (IOException ex) {
            logError("Cannot start process: " + ex.getMessage());
            return false;
        }

        LOG.info("Ethereum started successfully");
        return true;
    }

    private void setEmbeddedNodeAsCurrent() throws IOException {
        // make sure the embedded node is in the NodeInfo database
        String rpcUrl = gethConfig.getRpcUrl();
        String transactionManagerUrl = gethConfig.getGethTransactionManagerUrl();
        NodeInfo embeddedNodeInfo = nodeInfoDao
            .getByUrls(rpcUrl, transactionManagerUrl);
        if (embeddedNodeInfo == null) {
            LOG.debug("Couldn't find node in db, adding");
            nodeInfoDao.save(new NodeInfo("Embedded Node", rpcUrl,
                transactionManagerUrl));
        }
        connectToNode(rpcUrl, transactionManagerUrl);
    }

    @Override
    public void connectToNode(String rpcUrl, String transactionManagerUrl) {
        setCurrentRpcUrl(rpcUrl);
        setCurrentTransactionManagerUrl(transactionManagerUrl);
        runPostConnectTasks();
    }

    private void runPostConnectTasks() {
        if(blockScanner != null) {
            blockScanner.shutdown();
        }
        // run scanner thread
        blockScanner = applicationContext.getBean(BlockScanner.class);
        blockScanner.start();
    }

    @Override
    public List<String> setAdditionalParams(String[] additionalParamsArray) {
        List<String> additionalParams;
        if (null != additionalParamsArray && additionalParamsArray.length > 0) {
            additionalParams = Lists.newArrayList(additionalParamsArray);
        } else {
            additionalParams = new ArrayList<>();
        }
        boolean saveProps = false;
        //figure out if node is boot node
        if (gethConfig.isBootNode()) {
            if (StringUtils.isNotBlank(gethConfig.getBootNodeAddress())) {
                additionalParams.add("bootnode");
                additionalParams.add("--nodekeyhex");
                additionalParams.add(gethConfig.getBootNodeKey());
                additionalParams.add(" --addr");
                additionalParams.add(gethConfig.getBootNodeAddress());
            } else if (StringUtils.isNotBlank(System.getProperty("geth.bootnode.address"))
                    && StringUtils.isNotBlank(System.getProperty("geth.bootnode.key"))) {
                String nodeport = System.getProperty("geth.bootnode.address", "127.0.0.1:33445").split(":")[1];
                gethConfig.setBootNode("true");
                gethConfig.setBootNodeAddress(System.getProperty("geth.bootnode.address", "127.0.0.1:33445"));
                gethConfig.setBootNodeKey(System.getProperty("geth.bootnode.key"));
                gethConfig.setGethNodePort(nodeport);
                additionalParams.add("bootnode");
                additionalParams.add("--nodekeyhex");
                additionalParams.add(System.getProperty("geth.bootnode.key"));
                additionalParams.add(" --addr");
                additionalParams.add(System.getProperty("geth.bootnode.address", "127.0.0.1:33445"));
                saveProps = true;
            }
        } else {
            if (StringUtils.isNotBlank(gethConfig.getBootNodeList())) {
                additionalParams.add("--bootnodes");
                additionalParams.add(gethConfig.getBootNodeList());

            } else if (StringUtils.isNotBlank(System.getProperty("geth.bootnodes.list"))) {
                additionalParams.add("--bootnodes");
                additionalParams.add(System.getProperty("geth.bootnodes.list"));
                gethConfig.setBootNodeList(System.getProperty("geth.bootnodes.list"));
                saveProps = true;
            }

            //Set permissioned
            if (gethConfig.isPermissionedNode()) {
                additionalParams.add("--permissioned");
            } else if (StringUtils.isNotBlank(System.getProperty("geth.permissioned"))
                    && Boolean.valueOf(System.getProperty("geth.permissioned"))) {
                additionalParams.add("--permissioned");
                gethConfig.setPermissionedNode(Boolean.valueOf(System.getProperty("geth.permissioned")));
                saveProps = true;
            }

        }
        if (saveProps) {
            try {
                gethConfig.save();
            } catch (IOException e) {
                LOG.error("Error writing application.properties: " + e.getMessage());
                System.exit(1);
            }
        }

        return additionalParams;
    }

    /**
     * Initialize geth datadir via "geth init" command, using the configured
     * genesis block
     *
     * @return
     * @throws IOException
     */
    private boolean initGeth() throws IOException {
        ProcessBuilder builder = createProcessBuilder(gethRunner, createGethInitCommand());
        builder.inheritIO();
        try {
            Process process = builder.start();
            new StreamLogAdapter(GETH_LOG, process.getInputStream()).startAsync();
            new StreamLogAdapter(GETH_LOG, process.getErrorStream()).startAsync();

            int ret = process.waitFor();
            if (ret != 0) {
                logError("geth init returned non-zero exit code: " + ret);
            }
            return (ret == 0);
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for geth init", e);
            startupErrors.add(new ErrorLog("Interrupted while waiting for geth to init"));
        }
        return false;
    }

    private List<String> createGethInitCommand() {
        return Lists.newArrayList(gethRunner.getGethPath(),
                "--datadir", gethConfig.getGethDataDirPath(),
                "init", gethRunner.getGenesisBlockFilename()
        );
    }

    private List<String> createGethCommand(String... additionalParams) throws IOException {

        // Only unlock accounts from genesis file
        String accountsToUnlock = "";
        for (int i = 0; i < DEFAULT_NUMBER_ACCOUNTS; i++) {
            if (accountsToUnlock.length() > 0) {
                accountsToUnlock += ",";
            }
            accountsToUnlock += i;
        }

        List<String> commands = gethRunner.gethCommandLine();
        commands.add("--unlock");
        commands.add(accountsToUnlock);
        commands.add("--password");
        commands.add(gethRunner.getGethPasswordFile());

        if (null != additionalParams && additionalParams.length > 0) {
            commands.addAll(Lists.newArrayList(additionalParams));
        }

        if (!gethRunner.isEmbeddedQuorum()) {
            commands.add("--ipcdisable");
        }

        commands.add("--networkid");
        commands.add(String.valueOf(gethConfig.getNetworkId() == null ? DEFAULT_NETWORK_ID : gethConfig.getNetworkId()));

        commands.add("--verbosity");
        commands.add(String.valueOf(gethConfig.getVerbosity()));

        if (null != gethConfig.isMining() && gethConfig.isMining()) {
            commands.add("--mine");
            commands.add("--minerthreads");
            commands.add("1");
        }
        if (StringUtils.isNotEmpty(gethConfig.getIdentity())) {
            commands.add("--identity");
            commands.add(gethConfig.getIdentity());
        }

        // add custom params
        if (StringUtils.isNotBlank(gethConfig.getExtraParams())) {
            String[] params = gethConfig.getExtraParams().split(" ");
            for (String param : params) {
                if (StringUtils.isNotBlank(param)) {
                    commands.add(param);
                }
            }
        }

        return commands;
    }

    private boolean checkWalletUnlocked() {
        WalletService wallet = applicationContext.getBean(WalletService.class);
        List<Account> accounts;
        try {
            accounts = wallet.list();
        } catch (APIException e) {
            LOG.warn("Failed to list wallet accounts", e);
            startupErrors.add(new ErrorLog("Failed to list wallet accounts: " + ExceptionUtils.getMessage(e)));
            startupErrors.add(new ErrorLog(ExceptionUtils.getStackTrace(e)));
            return false;
        }

        long timeStart = System.currentTimeMillis();
        long timeout = gethConfig.getGethUnlockTimeout() * DEFAULT_NUMBER_ACCOUNTS; // default 2 sec per account

        LOG.info("Waiting up to " + timeout + "ms for " + DEFAULT_NUMBER_ACCOUNTS + " accounts to unlock");

        int unlocked = 0;

        try {
            TimeUnit.MILLISECONDS.sleep(timeout);
        } catch (InterruptedException e) {
            logError("Interrupted while waiting for wallet to unlock");
            return false;
        }

        //check if default accounts are unlocked
        for (Account account : accounts) {

            while (true && unlocked < DEFAULT_NUMBER_ACCOUNTS) {

                try {
                    if (wallet.isUnlocked(account.getAddress())) {
                        LOG.debug("Account " + account.getAddress() + " unlocked");
                        unlocked++;
                        break;
                    } else {
                        LOG.debug("Account " + account.getAddress() + " is NOT unlocked");
                    }
                } catch (APIException e) {
                    LOG.warn("Could not unlock address " + account.getAddress(), e);
                }

                if (System.currentTimeMillis() - timeStart >= timeout) {
                    logError("Wallet did not unlock in a timely manner ("
                            + unlocked + " of " + accounts.size() + " accounts unlocked)");
                    return false;
                }

            }
        }

        LOG.info("Geth wallet accounts unlocked (" + unlocked + " accounts)");
        return true;
    }

    private boolean checkGethStarted() {
        long timeStart = System.currentTimeMillis();

        while (true) {
            if (checkConnection()) {
                LOG.info("Geth RPC endpoint is up");
                return true;
            }

            if (System.currentTimeMillis() - timeStart >= gethConfig.getGethStartTimeout()) {
                // Something went wrong and RPC did not start within timeout
                logError("Geth RPC did not start within " + gethConfig.getGethStartTimeout() + "ms");
                break;
            }
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
        }

        return false;
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

    private void logError(String err) {
        LOG.error(err);
        startupErrors.add(new ErrorLog(err));
    }

    @Override
    public List<ErrorLog> getStartupErrors() {
        return ImmutableList.copyOf(startupErrors);
    }

}
