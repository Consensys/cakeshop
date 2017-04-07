package com.jpmorgan.cakeshop.service.impl;

import static com.jpmorgan.cakeshop.util.ProcessUtils.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.MediaType.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.jpmorgan.cakeshop.bean.QuorumConfigBean;
import com.jpmorgan.cakeshop.dao.BlockDAO;
import com.jpmorgan.cakeshop.dao.TransactionDAO;
import com.jpmorgan.cakeshop.dao.WalletDAO;
import com.jpmorgan.cakeshop.db.BlockScanner;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.error.ErrorLog;
import com.jpmorgan.cakeshop.model.Account;
import com.jpmorgan.cakeshop.model.RequestModel;
import com.jpmorgan.cakeshop.service.GethHttpService;
import com.jpmorgan.cakeshop.service.WalletService;
import com.jpmorgan.cakeshop.service.task.BlockchainInitializerTask;
import com.jpmorgan.cakeshop.service.task.LoadPeersTask;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.ProcessUtils;
import com.jpmorgan.cakeshop.util.StreamLogAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author Michael Kazansky
 */
@Service
public class GethHttpServiceImpl implements GethHttpService {

    public static final String SIMPLE_RESULT = "_result";
    public static final Integer DEFAULT_NETWORK_ID = 1006;
    public static final Integer DEFAULT_NUMBER_ACCOUNTS = 8;

    private static final Logger LOG = LoggerFactory.getLogger(GethHttpServiceImpl.class);
    private static final Logger GETH_LOG = LoggerFactory.getLogger("geth");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private GethConfigBean gethConfig;

    @Autowired
    private QuorumConfigBean quorumConfig;

    @Autowired(required = false)
    private BlockDAO blockDAO;

    @Autowired(required = false)
    private TransactionDAO txDAO;

    @Autowired(required = false)
    private WalletDAO walletDAO;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("asyncExecutor")
    private TaskExecutor executor;

    @Autowired
    private RestTemplate restTemplate;

    private BlockScanner blockScanner;

    private Boolean running;

    private StreamLogAdapter stdoutLogger;
    private StreamLogAdapter stderrLogger;

    private final List<ErrorLog> startupErrors;
    private final HttpHeaders jsonContentHeaders;

    public GethHttpServiceImpl() {
        this.running = false;
        this.startupErrors = new ArrayList<>();

        this.jsonContentHeaders = new HttpHeaders();
        this.jsonContentHeaders.setContentType(APPLICATION_JSON);
    }

    private String executeGethCallInternal(String json) throws APIException {
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("> " + json);
            }

            HttpEntity<String> httpEntity = new HttpEntity<>(json, jsonContentHeaders);
            ResponseEntity<String> response = restTemplate.exchange(gethConfig.getRpcUrl(), POST, httpEntity, String.class);

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
            return OBJECT_MAPPER.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new APIException("Failed to serialize request(s)", e);
        }
    }

    @Override
    public Map<String, Object> executeGethCall(String funcName, Object... args) throws APIException {
        return executeGethCall(new RequestModel(funcName, args, GETH_API_VERSION, GETH_REQUEST_ID));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> executeGethCall(RequestModel request) throws APIException {
        String response = executeGethCallInternal(requestToJson(request));

        if (StringUtils.isEmpty(response)) {
            throw new APIException("Received empty reply from server");
        }

        Map<String, Object> data;
        try {
            data = OBJECT_MAPPER.readValue(response, Map.class);
        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
        }

        return processResponse(data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> batchExecuteGethCall(List<RequestModel> requests) throws APIException {
        String response = executeGethCallInternal(requestToJson(requests));

        List<Map<String, Object>> responses;
        try {
            responses = OBJECT_MAPPER.readValue(response, List.class);

            List<Map<String, Object>> results = new ArrayList<>(responses.size());
            for (Map<String, Object> data : responses) {
                results.add(processResponse(data));
            }
            return results;

        } catch (IOException e) {
            throw new APIException("RPC call failed", e);
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

        if (gethConfig.isEmbeddedQuorum()) {
            if (!stopConstellation()) {
                LOG.error("Could not stop constellation");
            }
        }

        try {
            if (blockScanner != null) {
                blockScanner.shutdown();
            }

            if (stdoutLogger != null) {
                stdoutLogger.stopAsync();
            }

            if (stderrLogger != null) {
                stdoutLogger.stopAsync();
            }

            return killProcess(readPidFromFile(gethConfig.getGethPidFilename()), "geth.exe");

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

        this.deletePid();

        try {
            FileUtils.deleteDirectory(new File(gethConfig.getDataDirPath()));
        } catch (IOException ex) {
            LOG.error("Cannot delete directory " + ex.getMessage());
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

    @Override
    public Boolean deletePid() {
        return new File(gethConfig.getGethPidFilename()).delete();
    }

    @PreDestroy
    protected void autoStop() {
        if (!gethConfig.isAutoStop()) {
            return;
        }

        stop();
        deletePid();

        // stop solc server
        LOG.info("Stopping solc daemon");
        List<String> args = Lists.newArrayList(
                gethConfig.getNodePath(),
                gethConfig.getSolcPath(),
                "--stop-ipc");

        ProcessBuilder builder = ProcessUtils.createProcessBuilder(gethConfig, args);
        try {
            Process proc = builder.start();
            proc.waitFor();
        } catch (IOException | InterruptedException e) {
        }
    }

    @Override
    public Boolean isRunning() {
        if (gethConfig.isAutoStart()) {
            return running;
        }
        // always return true if we are not managing geth status
        // TODO add a healthcheck for external geth
        return true;
    }

    @Override
    public Boolean startConstellation() {
        Boolean success = false;
        File constellationLogDir = new File(quorumConfig.getConstellationConfigPath().concat("logs"));
        File constellationLog = new File(quorumConfig.getConstellationConfigPath().concat("logs/").concat("constellation.log"));
        if (!constellationLogDir.exists()) {
            constellationLogDir.mkdirs();
            if (!constellationLog.exists()) {
                try {
                    constellationLog.createNewFile();
                } catch (IOException ex) {
                    LOG.error("Could not create log for constellation", ex.getMessage());
                    return false;
                }
            }
        }
        //TODO: When Windows verision for constellation is available - add the functionality to start it under Windows.
        String[] command = new String[]{"/bin/sh", "-c",
            quorumConfig.getConstellationPath().concat(" ")
            .concat(quorumConfig.getConstellationConfigPath()).concat("node.conf")
            .concat(" 2>> ").concat(constellationLog.getAbsolutePath())
            .concat(" &")};
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            Process process = builder.start();
            Integer constProcessId = getUnixPID(process);
            writePidToFile(constProcessId, gethConfig.getConstPidFileName());
            success = true;
            LOG.info("CONSTELLATION STARTED");
            TimeUnit.SECONDS.sleep(5);
        } catch (IOException | InterruptedException ex) {
            LOG.error(ex.getMessage());
        }
        return success;
    }

    @Override
    public Boolean stopConstellation() {
        Boolean success = false;
        try {
            String pid = ProcessUtils.getUnixPidByName("constellation");
            if (StringUtils.isNotBlank(pid)) {
                success = killProcess(pid, null);
                LOG.info("Stopping Constellation with pid " + pid);
                new File(gethConfig.getConstPidFileName()).delete();
            } else {
                LOG.warn("Could not get PID to stop Constellation");
            }
        } catch (InterruptedException | IOException ex) {
            LOG.error(ex.getMessage());
        }
        return success;
    }

    @Override
    public Boolean start(String... additionalParams) {

        startupErrors.clear();

        if (isProcessRunning(readPidFromFile(gethConfig.getGethPidFilename()))) {
            LOG.info("Ethereum was already running; not starting again");
            return this.running = true;
        }

        try {
            String dataDir = gethConfig.getDataDirPath();

            // copy keystore if necessary
            File keystoreDir = new File(FileUtils.expandPath(dataDir, "keystore"));
            if (!keystoreDir.exists()) {
                LOG.debug("Initializing keystore");
                FileUtils.copyDirectory(new File(gethConfig.getKeystorePath()), keystoreDir);
            }

            // run geth init
            File chainDataDir = new File(FileUtils.expandPath(dataDir, "chaindata"));
            File newChainDataDir = new File(FileUtils.expandPath(dataDir, "geth", "chaindata"));
            if (!(chainDataDir.exists() || newChainDataDir.exists())) {
                //chainDataDir.mkdirs();
                LOG.debug("Running geth init");
                if (!initGeth()) {
                    logError("Geth datadir failed to initialize");
                    return this.running = false;
                }
            }

            if (gethConfig.isEmbeddedQuorum()) {
                additionalParams = setAdditionalParams(additionalParams).toArray(new String[setAdditionalParams(additionalParams).size()]);
                if (gethConfig.isConstellationEnabled() && !isProcessRunning(readPidFromFile(gethConfig.getConstPidFileName())) && !gethConfig.IS_BOOT_NODE) {
                    startConstellation();
                }
            }

            ProcessBuilder builder = createProcessBuilder(gethConfig, createGethCommand(additionalParams));
            final Map<String, String> env = builder.environment();

            if (gethConfig.isEmbeddedQuorum() && !gethConfig.IS_BOOT_NODE) {
                env.put("PRIVATE_CONFIG", quorumConfig.getConstellationConfigPath().concat("node.conf"));
            }

            Process process = builder.start();
            this.stdoutLogger = (StreamLogAdapter) new StreamLogAdapter(GETH_LOG, process.getInputStream()).startAsync();
            this.stderrLogger = (StreamLogAdapter) new StreamLogAdapter(GETH_LOG, process.getErrorStream()).startAsync();

            Integer pid = getProcessPid(process);
            if (pid != null) {
                writePidToFile(pid, gethConfig.getGethPidFilename());
            }

            if (!(checkGethStarted() && checkWalletUnlocked())) {
                logError("Ethereum failed to start");
                return this.running = false;
            }

            // TODO add a watcher thread to make sure it doesn't die..
        } catch (IOException ex) {
            logError("Cannot start process: " + ex.getMessage());
            return this.running = false;
        }

        runPostStartupTasks();

        LOG.info("Ethereum started successfully");
        return this.running = true;
    }

    @Override
    public void runPostStartupTasks() {
        // run chain init task
        BlockchainInitializerTask chainInitTask = applicationContext.getBean(BlockchainInitializerTask.class);
        chainInitTask.run(); // run in same thread

        // Reconnect peers on startup
        executor.execute(applicationContext.getBean(LoadPeersTask.class));

        // run scanner thread
        this.blockScanner = applicationContext.getBean(BlockScanner.class);
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
        Boolean isBootNode = false;
        Boolean saveProps = false;
        //figure out if node is boot node
        if (quorumConfig.isBootNode()) {
            if (StringUtils.isNotBlank(quorumConfig.getBootNodeAddress())) {
                additionalParams.add("bootnode");
                additionalParams.add("--nodekeyhex");
                additionalParams.add(quorumConfig.getBootNodeKey());
                additionalParams.add(" --addr");
                additionalParams.add(quorumConfig.getBootNodeAddress());
                isBootNode = true;
            } else if (StringUtils.isNotBlank(System.getProperty("geth.bootnode.address"))
                    && StringUtils.isNotBlank(System.getProperty("geth.bootnode.key"))) {
                String nodeport = System.getProperty("geth.bootnode.address", "127.0.0.1:33445").split(":")[1];
                gethConfig.setProperty("geth.boot.node", "true");
                gethConfig.setProperty("geth.bootnode.address", System.getProperty("geth.bootnode.address", "127.0.0.1:33445"));
                gethConfig.setProperty("geth.bootnode.key", System.getProperty("geth.bootnode.key"));
                gethConfig.setProperty("geth.node.port", nodeport);
                additionalParams.add("bootnode");
                additionalParams.add("--nodekeyhex");
                additionalParams.add(System.getProperty("geth.bootnode.key"));
                additionalParams.add(" --addr");
                additionalParams.add(System.getProperty("geth.bootnode.address", "127.0.0.1:33445"));
                saveProps = true;
                isBootNode = true;
            }
        }

        if (!isBootNode) {
            if (StringUtils.isNotBlank(quorumConfig.getBootNodes())) {
                additionalParams.add("--bootnodes");
                additionalParams.add(quorumConfig.getBootNodes());

            } else if (StringUtils.isNotBlank(System.getProperty("geth.bootnodes.list"))) {
                additionalParams.add("--bootnodes");
                additionalParams.add(System.getProperty("geth.bootnodes.list"));
                gethConfig.setProperty("geth.bootnodes.list", System.getProperty("geth.bootnodes.list"));
                saveProps = true;
            }

            //Set Block Maker account
            if (StringUtils.isNotBlank(gethConfig.getBlockMaker())) {
                additionalParams.add("--blockmakeraccount");
                additionalParams.add(gethConfig.getBlockMaker());
                additionalParams.add("--blockmakerpassword");
                additionalParams.add(null != gethConfig.getBlockMakerPass() ? gethConfig.getBlockMakerPass() : "");
            } else if (StringUtils.isNotBlank(System.getProperty("geth.block.maker"))) {
                additionalParams.add("--blockmakeraccount");
                additionalParams.add(System.getProperty("geth.block.maker"));
                additionalParams.add("--blockmakerpassword");
                String pass = StringUtils.isNotBlank(System.getProperty("geth.block.maker.pass")) ? System.getProperty("geth.block.maker.pass") : "";
                additionalParams.add(pass);
                gethConfig.setBlockMaker(System.getProperty("geth.block.maker"));
                gethConfig.setBlockMakerPass(pass);
                saveProps = true;
            }
            //Set min and max block time
            if (null != gethConfig.getMinBlockTime()) {
                additionalParams.add("--minblocktime");
                additionalParams.add(String.valueOf(gethConfig.getMinBlockTime()));
            } else if (StringUtils.isNotBlank(System.getProperty("geth.min.blocktime"))) {
                additionalParams.add("--minblocktime");
                additionalParams.add(System.getProperty("geth.min.blocktime"));
                gethConfig.setProperty("geth.min.blocktime", System.getProperty("geth.min.blocktime"));
                saveProps = true;
            } else {
                additionalParams.add("--minblocktime");
                additionalParams.add("2");
            }

            if (null != gethConfig.getMaxBlockTime()) {
                additionalParams.add("--maxblocktime");
                additionalParams.add(String.valueOf(gethConfig.getMaxBlockTime()));
            } else if (StringUtils.isNotBlank(System.getProperty("geth.max.blocktime"))) {
                additionalParams.add("--maxblocktime");
                additionalParams.add(System.getProperty("geth.max.blocktime"));
                gethConfig.setProperty("geth.max.blocktime", System.getProperty("geth.max.blocktime"));
                saveProps = true;
            } else {
                additionalParams.add("--maxblocktime");
                additionalParams.add("5");
            }

            //Set Vote Account
            if (StringUtils.isNotBlank(gethConfig.getVoteAccount())) {
                additionalParams.add("--voteaccount");
                additionalParams.add(gethConfig.getVoteAccount());
                additionalParams.add("--votepassword");
                additionalParams.add(null != gethConfig.getVoteAccountPass() ? gethConfig.getVoteAccountPass() : "");
            } else if (StringUtils.isNotBlank(System.getProperty("geth.vote.account"))) {
                additionalParams.add("--voteaccount");
                additionalParams.add(System.getProperty("geth.vote.account"));
                additionalParams.add("--votepassword");
                String pass = StringUtils.isNotBlank(System.getProperty("geth.vote.account.pass")) ? System.getProperty("geth.vote.account.pass") : "";
                additionalParams.add(pass);
                gethConfig.setVoteAccount(System.getProperty("geth.vote.account"));
                gethConfig.setVoteAccountPass(pass);
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
        ProcessBuilder builder = createProcessBuilder(gethConfig, createGethInitCommand());
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
        return Lists.newArrayList(gethConfig.getGethPath(),
                "--datadir", gethConfig.getDataDirPath(),
                "init", gethConfig.getGenesisBlockFilename()
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

        //Option to overwrite default port nide post and geth http usr through command line
        Boolean saveGethConfig = false;
        if (StringUtils.isNotBlank(System.getProperty("geth.url"))) {
            gethConfig.setRpcUrl(System.getProperty("geth.url"));
            saveGethConfig = true;
        }

        if (StringUtils.isNotBlank(System.getProperty("geth.node.port"))) {
            gethConfig.setGethNodePort(System.getProperty("geth.node.port"));
            saveGethConfig = true;
        }

        List<String> commands = Lists.newArrayList(gethConfig.getGethPath(),
                "--port", gethConfig.getGethNodePort(),
                "--datadir", gethConfig.getDataDirPath(),
                "--solc", gethConfig.getSolcPath(),
                "--nat", "none",
                "--nodiscover",
                "--unlock", accountsToUnlock, "--password", gethConfig.getGethPasswordFile(),
                "--rpc", "--rpcaddr", "127.0.0.1", "--rpcport", gethConfig.getRpcPort(),
                "--rpcapi", gethConfig.getRpcApiList()
        );

        if (null != additionalParams && additionalParams.length > 0) {
            commands.addAll(Lists.newArrayList(additionalParams));
        }

        if (!gethConfig.isEmbeddedQuorum()) {
            commands.add("--ipcdisable");
        }

        commands.add("--networkid");
        commands.add(String.valueOf(gethConfig.getNetworkId() == null ? DEFAULT_NETWORK_ID : gethConfig.getNetworkId()));

        commands.add("--verbosity");
        commands.add(String.valueOf(gethConfig.getVerbosity() == null ? "3" : gethConfig.getVerbosity()));

        if (null != gethConfig.isMining() && gethConfig.isMining() == true && !gethConfig.isEmbeddedQuorum()) {
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
        if (saveGethConfig) {
            gethConfig.save();
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
