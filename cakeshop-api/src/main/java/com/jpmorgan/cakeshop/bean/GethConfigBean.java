package com.jpmorgan.cakeshop.bean;

import static com.jpmorgan.cakeshop.util.FileUtils.*;
import static com.jpmorgan.cakeshop.util.ProcessUtils.*;

import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.SortedProperties;
import com.jpmorgan.cakeshop.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class GethConfigBean {

    private static final Logger LOG = LoggerFactory.getLogger(GethConfigBean.class);

    public static final String START_LINUX_COMMAND = "bin/linux/geth";
    public static final String START_WIN_COMMAND = "bin/win/geth.exe";
    public static final String START_MAC_COMMAND = "bin/mac/geth";

    @Autowired
    private Environment env;

    @Value("${config.path}")
    private String CONFIG_ROOT;

    @Autowired
    private QuorumConfigBean quorumConfig;

    private String configFile;

    private String binPath;

    private String gethPath;

    private String gethPidFilename;

    private String constPidFileName;

    private String gethPasswordFile;

    private String genesisBlockFilename;

    private String keystorePath;

    private String nodePath;

    private String solcPath;

    private String publicKey;

    /**
     * Whether or not this is a quorum node
     */
    private Boolean isQuorum;
    private boolean isEmbeddedQuorum;

    private Properties props;

    private static final String DEFAULT_NODE_PORT = "30303";

    private final String GETH_DATA_DIR = "geth.datadir";
    private final String GETH_LOG_DIR = "geth.log";
    private final String GETH_RPC_URL = "geth.url";
    private final String GETH_RPCAPI_LIST = "geth.rpcapi.list";
    private final String GETH_NODE_PORT = "geth.node.port";
    private final String GETH_AUTO_START = "geth.auto.start";
    private final String GETH_AUTO_STOP = "geth.auto.stop";
    private final String GETH_START_TIMEOUT = "geth.start.timeout";
    private final String GETH_UNLOCK_TIMEOUT = "geth.unlock.timeout";
    private final String EMBEDDED_NODE = null != System.getProperty("geth.node") ? System.getProperty("geth.node") : null;
    public final Boolean IS_BOOT_NODE = null != System.getProperty("geth.boot.node");

    //geth.db.enabled
    private final String GETH_DB_ENABLED = "cakeshop.database.vendor";

    // User-configurable settings
    private final String GETH_NETWORK_ID = "geth.networkid";
    private final String GETH_VERBOSITY = "geth.verbosity";
    private final String GETH_MINING = "geth.mining";
    private final String GETH_IDENTITY = "geth.identity";
    private final String GETH_EXTRA_PARAMS = "geth.params.extra";
    // Quorum specific settings
    private final String GETH_BLOCK_MAKER = "geth.block.maker";
    private final String GETH_VOTE_ACCOUNT = "geth.vote.account";
    private final String GETH_BLOCK_MAKER_PASS = "geth.block.maker.pass";
    private final String GETH_VOTE_ACCOUNT_PASS = "geth.vote.account.pass";
    private final String GETH_MIN_BLOCKTIME = "geth.min.blocktime";
    private final String GETH_MAX_BLOCKTIME = "geth.max.blocktime";
    private final String GETH_VOTE_CONTARCT_ADDRESS = "geth.vote.contract.addr";
    private final String GETH_CONSTELLATION_ENABLED = "geth.constellation.enabled";
    private final String GETH_PERMISSIONED = "geth.permissioned";

    public GethConfigBean() {
    }

    /**
     * Reset back to vendored config file and re-init bean config
     *
     * @throws IOException
     */
    public void initFromVendorConfig() throws IOException {
//        AppConfig.initVendorConfig(new File(configFile));
        initBean();
    }

    @PostConstruct
    private void initBean() {
        try {
            initGethBean();
        } catch (IOException | InterruptedException ex) {
            LOG.error(ex.getMessage());
        }
    }

    private void initGethBean() throws IOException, InterruptedException {

        // setup needed paths
        String baseResourcePath = System.getProperty("eth.geth.dir");
        if (StringUtils.isBlank(baseResourcePath)) {
            baseResourcePath = FileUtils.getClasspathName("geth");
        }

        // load props
        configFile = FileUtils.expandPath(CONFIG_ROOT, "application.properties");
        props = new Properties();
        props.load(new FileInputStream(configFile));

        // Choose correct geth binary
        if (SystemUtils.IS_OS_WINDOWS) {
            LOG.debug("Using geth for windows");
            gethPath = expandPath(baseResourcePath, START_WIN_COMMAND);
        } else if (SystemUtils.IS_OS_LINUX) {
            LOG.debug("Using geth for linux");
            gethPath = expandPath(baseResourcePath, START_LINUX_COMMAND);
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            LOG.debug("Using geth for mac");
            gethPath = expandPath(baseResourcePath, START_MAC_COMMAND);
        } else {
            LOG.error("Running on unsupported OS! Only Windows, Linux and Mac OS X are currently supported");
            throw new IllegalArgumentException("Running on unsupported OS! Only Windows, Linux and Mac OS X are currently supported");
        }

        if (!ensureFileIsExecutable(gethPath)) {
            throw new IOException("Path does not exist or is not executable: " + gethPath);
        }
        binPath = new File(gethPath).getParent();
        gethPidFilename = expandPath(CONFIG_ROOT, "geth.pid");

        // init genesis block file (using vendor copy if necessary)
        String vendorGenesisDir = expandPath(baseResourcePath, "genesis");

        genesisBlockFilename = expandPath(CONFIG_ROOT, "genesis_block.json");
        if (!new File(genesisBlockFilename).exists()) {
            String vendorGenesisBlockFile = FileUtils.join(vendorGenesisDir, "genesis_block.json");
            copyFile(new File(vendorGenesisBlockFile), new File(genesisBlockFilename));
        }

        if (SystemUtils.IS_OS_WINDOWS) {
            genesisBlockFilename = genesisBlockFilename.replaceAll(File.separator + File.separator, "/");
            if (genesisBlockFilename.startsWith("/")) {
                // fix filename like /C:/foo/bar/.../genesis_block.json
                genesisBlockFilename = genesisBlockFilename.substring(1);
            }
        }

        // set password file
        gethPasswordFile = expandPath(vendorGenesisDir, "geth_pass.txt");

        // set keystore path
        keystorePath = expandPath(vendorGenesisDir, "keystore");

        // configure node, solc
        ensureNodeBins(binPath);
        nodePath = FileUtils.expandPath(binPath, "node");
        if (SystemUtils.IS_OS_WINDOWS) {
            nodePath = nodePath + ".exe";
        }
        solcPath = expandPath(baseResourcePath, "solc", "node_modules", "solc-cakeshop-cli", "bin", "solc");
        ensureNodeBins(solcPath);
        // Clean up data dir path for default config (not an absolute path)
        if (getDataDirPath() != null) {
            if (getDataDirPath().startsWith("/.ethereum")) {
                // support old ~/.ethereum dir if it exists
                String path = expandPath(System.getProperty("user.home"), getDataDirPath());
                if (new File(path).exists()) {
                    setDataDirPath(path);
                } else {
                    setDataDirPath(expandPath(CONFIG_ROOT, "ethereum"));
                }
            } else {
                if (!new File(getDataDirPath()).exists()) {
                    setDataDirPath(expandPath(CONFIG_ROOT, "ethereum"));
                }
            }
        } else {
            // null, init it
            setDataDirPath(expandPath(CONFIG_ROOT, "ethereum"));
        }

        String identity = getIdentity();
        if (StringUtils.isBlank(identity)) {
            identity = System.getenv("USER");
            if (StringUtils.isBlank(identity)) {
                identity = System.getenv("USERNAME");
            }
        }
        setIdentity(identity);

        if (LOG.isDebugEnabled()) {
            LOG.debug(StringUtils.toString(this));
        }

        if (StringUtils.isBlank(EMBEDDED_NODE)) {
            // default to quorum
            setGethPath(quorumConfig.getQuorumPath());

            String destination = StringUtils.isNotBlank(System.getProperty("spring.config.location"))
                    ? System.getProperty("spring.config.location").replaceAll("file:", "")
                            .replaceAll("application.properties", "").concat("constellation-node/")
                    : getDataDirPath().concat("/constellation/");

            quorumConfig.createKeys("node", destination);
            quorumConfig.createQuorumConfig("node", destination);
            setConstPidFileName(expandPath(CONFIG_ROOT, "constellation.pid"));
            setIsEmbeddedQuorum(true);

            File pubKey = new File(destination.concat("node.pub"));
            try (Scanner scanner = new Scanner(pubKey)) {
                while (scanner.hasNext()) {
                    setPublicKey(scanner.nextLine());
                }
            }
        }
    }

    /**
     * Make sure all node bins are executable, both for win & mac/linux
     *
     * @param nodePath
     * @param solcPath
     */
    private void ensureNodeBins(String nodePath) {
        ensureFileIsExecutable(nodePath + File.separator + "node");
        ensureFileIsExecutable(nodePath + File.separator + "node.exe");
    }

    public String getGethPath() {
        return gethPath;
    }

    public void setGethPath(String gethPath) {
        this.gethPath = gethPath;
    }

    public String getGethPidFilename() {
        return gethPidFilename;
    }

    public void setGethPidFilename(String gethPidFilename) {
        this.gethPidFilename = gethPidFilename;
    }

    /**
     * @return the constPidFileName
     */
    public String getConstPidFileName() {
        return constPidFileName;
    }

    /**
     * @param constPidFileName the constPidFileName to set
     */
    public void setConstPidFileName(String constPidFileName) {
        this.constPidFileName = constPidFileName;
    }

    public String getDataDirPath() {
        return props.getProperty(GETH_DATA_DIR);
    }

    public void setDataDirPath(String dataDirPath) {
        props.setProperty(GETH_DATA_DIR, dataDirPath);
    }

    public String getLogDir() {
        return props.getProperty(GETH_LOG_DIR);
    }

    public void setLogDir(String logDir) {
        props.setProperty(GETH_LOG_DIR, logDir);
    }

    public String getGenesisBlockFilename() {
        return genesisBlockFilename;
    }

    public void setGenesisBlockFilename(String genesisBlockFilename) {
        this.genesisBlockFilename = genesisBlockFilename;
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

    public String getRpcApiList() {
        if (StringUtils.isBlank(EMBEDDED_NODE)) {
            if (props.getProperty(GETH_RPCAPI_LIST).contains("quorum")) {
                return props.getProperty(GETH_RPCAPI_LIST);
            } else {
                return props.getProperty(GETH_RPCAPI_LIST).concat(",").concat("quorum");
            }
        } else {
            return props.getProperty(GETH_RPCAPI_LIST);
        }
    }

    public void setRpcApiList(String rpcApiList) {
        if (StringUtils.isBlank(EMBEDDED_NODE)) {
            if (rpcApiList.contains("quorum")) {
                props.setProperty(GETH_RPCAPI_LIST, rpcApiList);
            } else {
                props.setProperty(GETH_RPCAPI_LIST, rpcApiList.concat(",").concat("quorum"));
            }
        } else {
            props.setProperty(GETH_RPCAPI_LIST, rpcApiList);
        }
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

    public Integer getNetworkId() {
        return Integer.valueOf(get(GETH_NETWORK_ID, "1006"));
    }

    public void setNetworkId(Integer networkId) {
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

    public String getBlockMaker() {
        return props.getProperty(GETH_BLOCK_MAKER);
    }

    public void setBlockMaker(String blockMaker) {
        props.setProperty(GETH_BLOCK_MAKER, blockMaker);
    }

    public String getBlockMakerPass() {
        return props.getProperty(GETH_BLOCK_MAKER_PASS);
    }

    public void setBlockMakerPass(String pass) {
        props.setProperty(GETH_BLOCK_MAKER_PASS, pass);
    }

    public String getVoteAccount() {
        return props.getProperty(GETH_VOTE_ACCOUNT);
    }

    public void setVoteAccount(String voteAccount) {
        props.setProperty(GETH_VOTE_ACCOUNT, voteAccount);
    }

    public String getVoteAccountPass() {
        return props.getProperty(GETH_VOTE_ACCOUNT_PASS);
    }

    public void setVoteAccountPass(String pass) {
        props.setProperty(GETH_VOTE_ACCOUNT_PASS, pass);
    }

    public Integer getMinBlockTime() {
        return StringUtils.isNotBlank(props.getProperty(GETH_MIN_BLOCKTIME)) ? Integer.valueOf(props.getProperty(GETH_MIN_BLOCKTIME)) : 2;
    }

    public Integer getMaxBlockTime() {
        return StringUtils.isNotBlank(props.getProperty(GETH_MAX_BLOCKTIME)) ? Integer.valueOf(props.getProperty(GETH_MAX_BLOCKTIME)) : 5;
    }

    public void setMinBlockTime(Integer minblockTime) {
        props.setProperty(GETH_MIN_BLOCKTIME, String.valueOf(minblockTime));
    }

    public void setMaxBlockTime(Integer maxblockTime) {
        props.setProperty(GETH_MAX_BLOCKTIME, String.valueOf(maxblockTime));
    }

    public String getVoteContractAddress() {
        return props.getProperty(GETH_VOTE_CONTARCT_ADDRESS);
    }

    public void setVoteContractAddress(String address) {
        props.setProperty(GETH_VOTE_CONTARCT_ADDRESS, address);
    }

    public Boolean isConstellationEnabled() {
        return StringUtils.isNotBlank(props.getProperty(GETH_CONSTELLATION_ENABLED))
                ? Boolean.valueOf(props.getProperty(GETH_CONSTELLATION_ENABLED))
                : Boolean.TRUE;
    }

    public void setConstellationEnabled(Boolean isEnabled) {
        props.setProperty(GETH_CONSTELLATION_ENABLED, String.valueOf(isEnabled));
    }

    public Boolean isPermissionedNode() {
        return StringUtils.isNotBlank(props.getProperty(GETH_PERMISSIONED))
                ? Boolean.valueOf(props.getProperty(GETH_PERMISSIONED))
                : Boolean.FALSE;
    }

    public void setPermissionedNode(Boolean isPermissionedNode) {
        props.setProperty(GETH_PERMISSIONED, String.valueOf(isPermissionedNode));
    }

    public String getBinPath() {
        return binPath;
    }

    public void setBinPath(String binPath) {
        this.binPath = binPath;
    }

    public String getGethPasswordFile() {
        return gethPasswordFile;
    }

    public void setGethPasswordFile(String gethPasswordFile) {
        this.gethPasswordFile = gethPasswordFile;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getSolcPath() {
        return solcPath;
    }

    public void setSolcPath(String solcPath) {
        this.solcPath = solcPath;
    }

    public String getExtraParams() {
        return props.getProperty(GETH_EXTRA_PARAMS);
    }

    public void setExtraParams(String extraParams) {
        props.setProperty(GETH_EXTRA_PARAMS, extraParams);
    }

    public String getGenesisBlock() throws IOException {
        return FileUtils.readFileToString(new File(genesisBlockFilename));
    }

    public void setGenesisBlock(String genesisBlock) throws IOException {
        FileUtils.writeStringToFile(new File(genesisBlockFilename), genesisBlock);
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }

    public boolean isDbEnabled() {
        return StringUtils.isNotBlank(env.getProperty(GETH_DB_ENABLED)) || StringUtils.isNotBlank(System.getProperty(GETH_DB_ENABLED));
    }

    public void setDbEnabled(String vendor) {
        props.setProperty(GETH_DB_ENABLED, vendor);
    }

    public int getGethStartTimeout() {
        return Integer.parseInt(get(GETH_START_TIMEOUT, "10000"));
    }

    public void setGethStartTimeout(int timeout) {
        props.setProperty(GETH_START_TIMEOUT, Integer.toString(timeout));
    }

    public int getGethUnlockTimeout() {
        return Integer.parseInt(get(GETH_UNLOCK_TIMEOUT, "2000"));
    }

    public void setGethUnlockTimeout(int timeout) {
        props.setProperty(GETH_UNLOCK_TIMEOUT, Integer.toString(timeout));
    }

    public Boolean isQuorum() {
        return isQuorum;
    }

    public void setQuorum(Boolean isQuorum) {
        this.isQuorum = isQuorum;
    }

    /**
     * Write the underlying config file to disk (persist all properties)
     *
     * @throws IOException
     */
    public void save() throws IOException {
        SortedProperties.store(props, new FileOutputStream(configFile));
    }

    /**
     * Write a property directly to the underlying property store
     *
     * @param key
     * @param val
     */
    public void setProperty(String key, String val) {
        props.setProperty(key, val);
    }

    /**
     * Simple wrapper around {@link Properties#getProperty(String)} which
     * handles empty strings and nulls properly
     *
     * @param key
     * @param defaultStr
     * @return
     */
    private String get(String key, String defaultStr) {
        return StringUtils.defaultIfBlank(props.getProperty(key), defaultStr);
    }

    /**
     * @return the isEmbeddedQuorum
     */
    public boolean isEmbeddedQuorum() {
        return isEmbeddedQuorum;
    }

    /**
     * @param isEmbeddedQuorum the isEmbeddedQuorum to set
     */
    public void setIsEmbeddedQuorum(boolean isEmbeddedQuorum) {
        this.isEmbeddedQuorum = isEmbeddedQuorum;
    }

    /**
     * @return the publicKey
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey the publicKey to set
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

}
