package com.jpmorgan.cakeshop.bean;

import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.util.DownloadUtils;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.SortedProperties;
import com.jpmorgan.cakeshop.util.StringUtils;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.SslAuthenticationMode;
import com.quorum.tessera.config.SslTrustMode;
import com.quorum.tessera.config.builder.ConfigBuilder;
import com.quorum.tessera.config.builder.KeyDataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

@Component
public class GethConfig {

    private static final Logger LOG = LoggerFactory.getLogger(GethConfig.class);
    private static final String DEFAULT_NODE_PORT = "30303";

    public static final String GETH_DATA_DIR = "geth.datadir";
    public static final String GETH_LOG_DIR = "geth.log";
    public static final String GETH_RPC_URL = "geth.url";
    public static final String GETH_RPCAPI_LIST = "geth.rpcapi.list";
    public static final String GETH_NODE_PORT = "geth.node.port";
    public static final String GETH_AUTO_START = "geth.auto.start";
    public static final String GETH_AUTO_STOP = "geth.auto.stop";
    public static final String GETH_START_TIMEOUT = "geth.start.timeout";
    public static final String GETH_UNLOCK_TIMEOUT = "geth.unlock.timeout";
    public static final String EMBEDDED_NODE = System.getProperty("geth.node");

    //geth.db.enabled
    public static final String GETH_DB_ENABLED = "cakeshop.database.vendor";

    // User-configurable settings
    public static final String GETH_NETWORK_ID = "geth.networkid";
    public static final String GETH_VERBOSITY = "geth.verbosity";
    public static final String GETH_MINING = "geth.mining";
    public static final String GETH_IDENTITY = "geth.identity";
    public static final String GETH_EXTRA_PARAMS = "geth.params.extra";
    // Quorum specific settings
    public static final String GETH_PERMISSIONED = "geth.permissioned";
    public static final String GETH_RAFT_PORT = "geth.raft.port";
    public static final String GETH_RAFT_BLOCKTIME = "geth.raft.blocktime";
    public static final String GETH_CONSENSUS_MODE = "geth.consensus.mode";
    public static final String GETH_STARTUP_MODE = "geth.startup.mode";
    public static final String GETH_RAFT_NETWORK_ID = "geth.raft.network.id";

    public static final String GETH_TRANSACTION_MANAGER_TYPE = "geth.transaction_manager.type";
    public static final String GETH_TRANSACTION_MANAGER_URL = "geth.transaction_manager.url";
    public static final String GETH_TRANSACTION_MANAGER_PEERS = "geth.transaction_manager.peers";
    public static final String GETH_BOOT_NODE = "geth.boot.node";
    public static final String GETH_BOOTNODE_ADDRESS = "geth.bootnode.address";
    public static final String GETH_BOOTNODE_KEY = "geth.bootnode.key";
    public static final String GETH_BOOTNODES_LIST = "geth.bootnodes.list";
    public static final String CONTRACT_REGISTRY_ADDR = "contract.registry.addr";

    // Binary download urls and binary names
    public static final String QUORUM_RELEASE_URL = "geth.quorum.release.url";
    public static final String GETH_RELEASE_URL = "geth.release.url";
    public static final String NODE_BINARY_NAME = "nodejs.binary";


    @Value("${config.path}")
    private String dataDirectory;

    @Autowired
    private Environment env;

    private Properties props;

    private String customSpringConfigPath;

    private TransactionManager.Type transactionManagerType;

    public GethConfig() {
    }

    @PostConstruct
    private void initBean() {
        try {
            initGethConfig();
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void initGethConfig() throws IOException {
        // load props
        customSpringConfigPath = FileUtils.expandPath(dataDirectory, "application.properties");
        props = new Properties();
        props.load(new FileInputStream(customSpringConfigPath));

        transactionManagerType = TransactionManager.Type
            .valueOf(getGethTransactionManagerType().toLowerCase());
        LOG.debug("Using transaction manager: {}", transactionManagerType);
        handleCommandLineOverrides();

    }

    private void handleCommandLineOverrides() throws IOException {
        //Option to overwrite default port nide post and geth http usr through command line
        Boolean saveGethConfig = false;
        if (StringUtils.isNotBlank(System.getProperty(GETH_RPC_URL))) {
            setRpcUrl(System.getProperty(GETH_RPC_URL));
            saveGethConfig = true;
        }

        if (StringUtils.isNotBlank(System.getProperty(GETH_NODE_PORT))) {
            setGethNodePort(System.getProperty(GETH_NODE_PORT));
            saveGethConfig = true;
        }

        if (StringUtils.isNotBlank(System.getProperty(GethConfig.GETH_RAFT_PORT))) {
            setRaftPort(System.getProperty(GethConfig.GETH_RAFT_PORT));
            saveGethConfig = true;
        }

        if (StringUtils.isNotBlank(System.getProperty(GethConfig.GETH_TRANSACTION_MANAGER_URL))) {
            setGethTransactionManagerUrl(System.getProperty(GethConfig.GETH_TRANSACTION_MANAGER_URL));
            saveGethConfig = true;
        }

        if (StringUtils.isNotBlank(System.getProperty(GethConfig.GETH_TRANSACTION_MANAGER_TYPE))) {
            setGethTransactionManagerUrl(System.getProperty(GethConfig.GETH_TRANSACTION_MANAGER_TYPE));
            saveGethConfig = true;
        }

        if (StringUtils.isNotBlank(System.getProperty("server.port"))) {
            setCakeshopPort(System.getProperty("server.port"));
            saveGethConfig = true;
        }
        if (saveGethConfig) {
            save();
        }
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public boolean shouldUseQuorum() {
        return StringUtils.isBlank(EMBEDDED_NODE) || EMBEDDED_NODE.equalsIgnoreCase("quorum");
    }

    public String getGethDataDirPath() {
        return props.getProperty(GETH_DATA_DIR);
    }

    public void setGethDataDirPath(String dataDirPath) {
        props.setProperty(GETH_DATA_DIR, dataDirPath);
    }

    public String getLogDir() {
        return props.getProperty(GETH_LOG_DIR);
    }

    public void setLogDir(String logDir) {
        props.setProperty(GETH_LOG_DIR, logDir);
    }

    public String getRpcUrl() {
        return props.getProperty(GETH_RPC_URL);
    }

    public void setRpcUrl(String rpcUrl) {
        props.setProperty(GETH_RPC_URL, rpcUrl);
    }

    public String getRpcPort() {
        String url = getRpcUrl();
        if (StringUtils.isBlank(url)) {
            return null;
        }
        URI uri = URI.create(url);
        return Integer.toString(uri.getPort());
    }

    public String getRaftPort() {
        return props.getProperty(GETH_RAFT_PORT);
    }

    public void setRaftPort(String port) {
        props.setProperty(GETH_RAFT_PORT, port);
    }

    public int getRaftBlockFrequency() {
        return Integer.parseInt(props.getProperty(GETH_RAFT_BLOCKTIME));
    }

    public String getRpcApi() {
        return props.getProperty(GETH_RPCAPI_LIST);
    }

    public void setRpcApiList(String list) {
        HashSet<String> apiset = new HashSet<String>(Arrays.asList(list.split(",")));

        props.setProperty(GETH_RPCAPI_LIST, String.join(",", apiset.toArray(new String[0])));
    }


    /**
     * Minting frequency expressed in ms
     */
    public void setRaftBlockFrequency(int frequency) {
        props.setProperty(GETH_RAFT_BLOCKTIME, frequency + "");
    }

    public String getConsensusMode() {
        return get(GETH_CONSENSUS_MODE, "raft");
    }

    /**
     * Set Consensus Mode, default to raft, valid options are raft, istanbul
     */
    public void setConsensusMode(String mode) {
        if (null == mode || mode.trim().isEmpty()) {
            props.setProperty(GETH_CONSENSUS_MODE, "raft");
        } else if (mode.equalsIgnoreCase("istanbul")) { // TODO: Mode should be an enum
            props.setProperty(GETH_CONSENSUS_MODE, "istanbul");
        } else {
            props.setProperty(GETH_CONSENSUS_MODE, "raft");
        }
    }

    public String getStartupMode() {
        return get(GETH_STARTUP_MODE, "standalone");
    }

    /**
     * Set Startup Mode, default to standalone, valid options are stanalone, join. Joining an
     * existing network requires additional parameters
     */
    public void setStartupMode(String mode) {
        if (null == mode || mode.trim().isEmpty()) {
            props.setProperty(GETH_STARTUP_MODE, "standalone");
        } else if (mode.equalsIgnoreCase("join")) { // TODO: Mode should be an enum
            props.setProperty(GETH_STARTUP_MODE, "join");
        } else {
            props.setProperty(GETH_STARTUP_MODE, "standalone");
        }
    }

    public void setRaftNetworkId(String id) {
        props.setProperty(GETH_RAFT_NETWORK_ID, id);
    }

    public String getRaftNetworkId() {
        return get(GETH_RAFT_NETWORK_ID, "");
    }

    public String getGethNodePort() {
        return props.getProperty(GETH_NODE_PORT, DEFAULT_NODE_PORT);
    }

    public void setGethNodePort(String gethNodePort) {
        props.setProperty(GETH_NODE_PORT, gethNodePort);
    }

    public Boolean isAutoStart() {
        return Boolean.valueOf(get(GETH_AUTO_START, "false"));
    }

    public void setAutoStart(Boolean autoStart) {
        props.setProperty(GETH_AUTO_START, autoStart.toString());
    }

    public Boolean isAutoStop() {
        return Boolean.valueOf(get(GETH_AUTO_STOP, "false"));
    }

    public void setAutoStop(Boolean autoStop) {
        props.setProperty(GETH_AUTO_STOP, autoStop.toString());
    }

    public Long getNetworkId() {
        return Long.valueOf(get(GETH_NETWORK_ID, "1006"));
    }

    public void setNetworkId(Long networkId) {
        props.setProperty(GETH_NETWORK_ID, networkId.toString());
    }

    public Integer getVerbosity() {
        return Integer.valueOf(get(GETH_VERBOSITY, "3"));
    }

    public void setVerbosity(Integer verbosity) {
        props.setProperty(GETH_VERBOSITY, verbosity.toString());
    }

    public Boolean isMining() {
        return Boolean.valueOf(get(GETH_MINING, "false"));
    }

    public void setMining(Boolean mining) {
        props.setProperty(GETH_MINING, mining.toString());
    }

    public String getIdentity() {
        return props.getProperty(GETH_IDENTITY);
    }

    public void setIdentity(String identity) {
        props.setProperty(GETH_IDENTITY, identity);
    }

    public Boolean isTransactionManagerEnabled() {
        return !getGethTransactionManagerType()
            .equals(TransactionManager.Type.none.transactionManagerName);
    }

    public Boolean isPermissionedNode() {
        return Boolean.valueOf(get(GETH_PERMISSIONED, "false"));
    }

    public void setPermissionedNode(Boolean isPermissionedNode) {
        props.setProperty(GETH_PERMISSIONED, String.valueOf(isPermissionedNode));
    }

    public String getExtraParams() {
        return props.getProperty(GETH_EXTRA_PARAMS);
    }

    public void setExtraParams(String extraParams) {
        props.setProperty(GETH_EXTRA_PARAMS, extraParams);
    }

    public int getGethStartTimeout() {
        return Integer.parseInt(get(GETH_START_TIMEOUT, "10000"));
    }

    public void setGethStartTimeout(int timeout) {
        props.setProperty(GETH_START_TIMEOUT, Integer.toString(timeout));
    }

    public int getGethUnlockTimeout() {
        return Integer.parseInt(get(GETH_UNLOCK_TIMEOUT, "5000"));
    }

    public void setGethUnlockTimeout(int timeout) {
        props.setProperty(GETH_UNLOCK_TIMEOUT, Integer.toString(timeout));
    }

    public boolean isDbEnabled() {
        return StringUtils.isNotBlank(env.getProperty(GETH_DB_ENABLED)) || StringUtils
            .isNotBlank(System.getProperty(GETH_DB_ENABLED));
    }

    public void setDbEnabled(String vendor) {
        props.setProperty(GETH_DB_ENABLED, vendor);
    }

    public Boolean isBootNode() {
        return Boolean.valueOf(props.getProperty(GETH_BOOT_NODE));
    }

    public void setBootNode(String isBootNode) {
        props.setProperty(GETH_BOOT_NODE, isBootNode.toString());
    }

    public String getBootNodeAddress() {
        return props.getProperty(GETH_BOOTNODE_ADDRESS);
    }

    public void setBootNodeAddress(String addr) {
        props.setProperty(GETH_BOOTNODE_ADDRESS, addr);
    }

    public String getBootNodeKey() {
        return props.getProperty(GETH_BOOTNODE_KEY);
    }

    public void setBootNodeKey(String key) {
        props.setProperty(GETH_BOOTNODE_KEY, key);
    }

    public String getBootNodeList() {
        return props.getProperty(GETH_BOOTNODES_LIST);
    }

    public void setBootNodeList(String bootNodeList) {
        props.setProperty(GETH_BOOTNODES_LIST, bootNodeList);
    }

    public String getContractAddress() {
        return props.getProperty(CONTRACT_REGISTRY_ADDR);
    }

    public void setContractAddress(String addr) {
        props.setProperty(CONTRACT_REGISTRY_ADDR, addr);
    }

    public String getGethTransactionManagerType() {
        return get(GETH_TRANSACTION_MANAGER_TYPE, "tessera");
    }

    public void setGethTransactionManagerType(String type) {
        props.setProperty(GETH_TRANSACTION_MANAGER_TYPE, type);
    }

    public String getGethTransactionManagerUrl() {
        return get(GETH_TRANSACTION_MANAGER_URL, "http://127.0.0.1:9000/");
    }

    public void setGethTransactionManagerUrl(String url) {
        props.setProperty(GETH_TRANSACTION_MANAGER_URL, url);
    }

    public List<String> getGethTransactionManagerPeers() {
        return Lists
            .newArrayList(get(GETH_TRANSACTION_MANAGER_PEERS, "http://localhost:9000/").split(","));
    }

    public void setGethTransactionManagerPeers(List<String> peers) {
        props.setProperty(GETH_TRANSACTION_MANAGER_PEERS, String.join(",", peers));
    }

    public TransactionManager.Type getTransactionManagerType() {
        return transactionManagerType;
    }

    public String getCakeshopPort() {
        return props.getProperty("server.port", "8080");
    }

    public void setCakeshopPort(String port) {
        props.setProperty("server.port", port);
    }

    /**
     * Write the underlying config file to disk (persist all properties)
     */
    public void save() throws IOException {
        SortedProperties.store(props, new FileOutputStream(customSpringConfigPath));
    }

    /**
     * Allows overrides of application properies with system properties, returning the default value
     * if property is not defined.
     */
    private String get(String key, String defaultStr) {
        if (StringUtils.isNotBlank(System.getProperty(key))) {
            return System.getProperty(key);
        }
        if (StringUtils.isNotBlank(props.getProperty(key))) {
            return props.getProperty(key);
        }
        return defaultStr;
    }

    public String getQuorumReleaseUrl() {
        return get(QUORUM_RELEASE_URL, DownloadUtils.getDefaultQuorumReleaseUrl());
    }

    public String getGethReleaseUrl() {
        return get(GETH_RELEASE_URL, DownloadUtils.getDefaultGethReleaseUrl());
    }

    public String getNodeJsBinaryName() {
        return get(NODE_BINARY_NAME, "node");
    }

    public String getTransactionManagerDataPath() {
        return FileUtils.expandPath(getDataDirectory(),
            getTransactionManagerType().transactionManagerName);
    }

    public Config getTesseraConfig() throws MalformedURLException {
        String prefix = Paths.get(getTransactionManagerDataPath(),
            TransactionManager.Type.TRANSACTION_MANAGER_KEY_NAME).toString();
        URL url = new URL(getGethTransactionManagerUrl());
        JdbcConfig jdbcConfig = new JdbcConfig("", "", "jdbc:h2:mem:tessera");
        jdbcConfig.setAutoCreateTables(true);
        return ConfigBuilder.create()
            .unixSocketFile(prefix + ".ipc")
            .useWhiteList(false)
            .jdbcConfig(jdbcConfig)
            .keyData(
                KeyDataBuilder.create()
                    .withPublicKeys(Collections.singletonList(prefix + ".pub"))
                    .withPrivateKeys(Collections.singletonList(prefix + ".key"))
                    .build()
            )
            .peers(getGethTransactionManagerPeers())
            .serverHostname(url.getProtocol() + "://" + url.getHost())
            .serverPort(url.getPort())
            .sslAuthenticationMode(SslAuthenticationMode.OFF)
            .sslClientTrustMode(SslTrustMode.TOFU)
            .sslClientTrustMode(SslTrustMode.TOFU)
            .build();
    }

    public boolean isRaft() {
        return getConsensusMode().equals("raft");
    }
}
