package com.jpmorgan.cakeshop.bean;

import static com.jpmorgan.cakeshop.util.FileUtils.expandPath;
import static com.jpmorgan.cakeshop.util.ProcessUtils.ensureFileIsExecutable;
import static org.apache.commons.io.FileUtils.copyFile;

import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.ProcessUtils;
import com.jpmorgan.cakeshop.util.StringUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GethRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GethRunner.class);

    public static final String START_GETH_COMMAND =
        "geth/" + ProcessUtils.getPlatformDirectory() + "/geth";
    public static final String START_QUORUM_COMMAND =
        "quorum/" + ProcessUtils.getPlatformDirectory() + "/geth";

    @Autowired
    private GethConfig gethConfig;

    @Autowired
    private TransactionManagerRunner transactionManagerRunner;

    private String gethPidFilename;

    private String transactionManagerPidFileName;

    private String gethPasswordFile;

    private String genesisBlockFilename;

    private String keystorePath;

    private String nodeJsPath;

    private String solcPath;

    private String publicKey;

    /**
     * Whether or not this is a quorum node
     */
    private Boolean isQuorum;
    private Boolean isEmbeddedQuorum;

    private String binPath;

    /**
     * Reset back to vendored config file and re-init bean config
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
            LOG.error(ex.getMessage(), ex);
        }
    }

    // TODO do we really need to do all this at bean initialization? Or can we do it before start()
    private void initGethBean() throws IOException, InterruptedException {

        // setup needed paths
        binPath = System.getProperty("eth.bin.dir");
        if (StringUtils.isBlank(binPath)) {
            binPath = FileUtils.getClasspathName("bin");
        }

        gethPidFilename = expandPath(gethConfig.getDataDirectory(), "geth.pid");

        // init genesis block file (using vendor copy if necessary)
        String vendorGenesisDir = expandPath(binPath,
            "genesis"); // TODO: this block is redundant now
        genesisBlockFilename = expandPath(gethConfig.getDataDirectory(), "genesis_block.json");
        String vendorGenesisBlockFile = FileUtils
            .join(vendorGenesisDir, gethConfig.getConsensusMode() + "_genesis_block.json");
        copyFile(new File(vendorGenesisBlockFile), new File(genesisBlockFilename));

        if (SystemUtils.IS_OS_WINDOWS) {
            genesisBlockFilename = genesisBlockFilename
                .replaceAll(File.separator + File.separator, "/");
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
        nodeJsPath = FileUtils
            .expandPath(binPath, "nodejs", ProcessUtils.getPlatformDirectory(), "node");
        if (SystemUtils.IS_OS_WINDOWS) {
            nodeJsPath = nodeJsPath + ".exe";
        }
        ensureFileIsExecutable(nodeJsPath);

        solcPath = expandPath(binPath, "solc", "node_modules", "solc-cakeshop-cli", "bin",
            "solc");
        ensureFileIsExecutable(solcPath);

        // Clean up data dir path for default config (not an absolute path)
        if (gethConfig.getGethDataDirPath() != null) {
            if (gethConfig.getGethDataDirPath().startsWith("/.ethereum")) {
                // support old ~/.ethereum dir if it exists
                String path = expandPath(System.getProperty("user.home"),
                    gethConfig.getGethDataDirPath());
                if (new File(path).exists()) {
                    gethConfig.setGethDataDirPath(path);
                } else {
                    gethConfig
                        .setGethDataDirPath(expandPath(gethConfig.getDataDirectory(), "ethereum"));
                }
            } else {
                if (!new File(gethConfig.getGethDataDirPath()).exists()) {
                    gethConfig
                        .setGethDataDirPath(expandPath(gethConfig.getDataDirectory(), "ethereum"));
                }
            }
        } else {
            // null, init it
            gethConfig.setGethDataDirPath(expandPath(gethConfig.getDataDirectory(), "ethereum"));
        }

        // Initialize node identity
        String identity = gethConfig.getIdentity();

        if (StringUtils.isBlank(identity)) {
            identity = System.getenv("USER");

            if (StringUtils.isBlank(identity)) {
                identity = System.getenv("USERNAME");
            }

            // No idenity set, and user info missing in the env prefs
            if (StringUtils.isBlank(identity)) {
                LOG.error(
                    "Node identity preference is missing, please ensure geth.identity is set in application properties");
                throw new IllegalArgumentException(
                    "Node identity preference is missing, please ensure geth.identity is set in application properties");
            }
        }

        gethConfig.setIdentity(identity);

        if (LOG.isDebugEnabled()) {
            LOG.debug(StringUtils.toString(this));
        }

        if (gethConfig.shouldUseQuorum()) {
            setQuorum(true);
            setIsEmbeddedQuorum(true);

            if (gethConfig.getTransactionManagerType() != TransactionManager.Type.none) {
                String publicKeyPath = transactionManagerRunner
                    .createTransactionManagerNodeKeys();
                transactionManagerRunner.writeTransactionManagerConfig();

                File pubKey = new File(publicKeyPath);
                try (Scanner scanner = new Scanner(pubKey)) {
                    while (scanner.hasNext()) {
                        setPublicKey(scanner.nextLine());
                    }
                }
            }
        }

        LOG.debug("Using geth at {}", getGethPath());

        if (!ensureFileIsExecutable(getGethPath())) {
            throw new IOException("Path does not exist or is not executable: " + getGethPath());
        }
    }

    public void initializeConsensusMode() throws IOException {
        createStaticNodesConfig();
        if (gethConfig.getConsensusMode().equalsIgnoreCase("istanbul")) {
            updateIstanbulGenesis();
        } else if (gethConfig.getConsensusMode().equalsIgnoreCase("raft")) {
            updateRaftGenesis();
        }
    }

    public String getGethPath() {
        return expandPath(binPath,
            gethConfig.shouldUseQuorum() ? START_QUORUM_COMMAND : START_GETH_COMMAND);
    }

    public String getGethPidFilename() {
        return gethPidFilename;
    }

    public void setGethPidFilename(String gethPidFilename) {
        this.gethPidFilename = gethPidFilename;
    }

    public String getGenesisBlockFilename() {
        return genesisBlockFilename;
    }

    public void setGenesisBlockFilename(String genesisBlockFilename) {
        this.genesisBlockFilename = genesisBlockFilename;
    }

    public String getRpcApiList() {
        return getRpcApiList("raft");
    }

    public String getRpcApiList(String mode) {
        HashSet<String> apiset = new HashSet<String>(
            Arrays.asList(gethConfig.getRpcApi().split(",")));

        if (null == mode || mode.trim().isEmpty() || mode.equalsIgnoreCase("raft")) {
            apiset.remove("istanbul");
            apiset.add("raft");
            return String.join(",", apiset.toArray(new String[0]));
        } else if (mode.equalsIgnoreCase("istanbul")) {
            apiset.add("istanbul");
            apiset.remove("raft");
            return String.join(",", apiset.toArray(new String[0]));
        } else {
            return getRpcApiList("raft");
        }
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


    public String getGenesisBlock() throws IOException {
        return FileUtils.readFileToString(new File(genesisBlockFilename));
    }

    public void setGenesisBlock(String genesisBlock) throws IOException {
        FileUtils.writeStringToFile(new File(genesisBlockFilename), genesisBlock);
    }

    public String getNodeJsPath() {
        return nodeJsPath;
    }

    public void setNodeJsPath(String nodeJsPath) {
        this.nodeJsPath = nodeJsPath;
    }

    public boolean isQuorum() {
        return isQuorum;
    }

    public void setQuorum(Boolean isQuorum) {
        this.isQuorum = isQuorum;
    }


    /**
     * @return the isEmbeddedQuorum
     */
    public Boolean isEmbeddedQuorum() {
        return isEmbeddedQuorum;
    }

    /**
     * @param isEmbeddedQuorum the isEmbeddedQuorum to set
     */
    public void setIsEmbeddedQuorum(boolean isEmbeddedQuorum) {
        this.isEmbeddedQuorum = isEmbeddedQuorum;
    }

    /**
     * @return Constellation public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Returns the location of the Geth nodekey as generated by `bootnode`
     */
    private Path verifyNodeKey() throws IOException {
        Path nodekeypath = Paths.get(gethConfig.getGethDataDirPath(), "geth", "nodekey");

        if (Files.exists(nodekeypath)) {
            return nodekeypath;
        }

        if (!Files.exists(nodekeypath.getParent())) {
            nodekeypath.getParent().toFile().mkdirs();
        }

        Path bootnodelocation = Paths
            .get(Paths.get(getGethPath()).getParent().toString(), "bootnode");

        File bootnodebinary = bootnodelocation.toFile();
        if (!bootnodebinary.canExecute()) {
            bootnodebinary.setExecutable(true);
        }

        List<String> bootnodeparams = Lists
            .newArrayList(bootnodelocation.toString(), "-genkey", nodekeypath.toString());
        ProcessBuilder builder = new ProcessBuilder(bootnodeparams);
        LOG.info("generating nodekey as " + String.join(" ", builder.command()));
        Process process = builder.start();

        try {
            process.waitFor(5, TimeUnit.SECONDS);
        } catch (Exception e) {
        }

        if (process.isAlive()) {
            process.destroy();
        }

        return nodekeypath;
    }

    private String getLocalEthereumAddress() throws IOException {
        Path nodekeypath = verifyNodeKey();
        String localnodeaddress = "";

        Path bootnodelocation = Paths
            .get(Paths.get(getGethPath()).getParent().toString(), "bootnode");

        File bootnodebinary = bootnodelocation.toFile();
        if (!bootnodebinary.canExecute()) {
            bootnodebinary.setExecutable(true);
        }

        List<String> bootnodeparams = Lists
            .newArrayList(bootnodelocation.toString(), "-nodekey", nodekeypath.toString(),
                "-writeaddress");
        ProcessBuilder builder = new ProcessBuilder(bootnodeparams);
        LOG.info("generating local address as " + String.join(" ", builder.command()));
        Process process = builder.start();

        try (Scanner scanner = new Scanner(process.getInputStream())) {
            localnodeaddress = scanner.next();
            if (localnodeaddress.contains("Fatal")) {
                while (scanner.hasNext()) {
                    LOG.error(scanner.next());
                }
                localnodeaddress = null;
            }
        }

        if (process.isAlive()) {
            process.destroy();
        }

        return localnodeaddress;
    }

    /**
     *
     */
    private void createStaticNodesConfig() throws IOException {
        Path staticnodespath = Paths.get(gethConfig.getGethDataDirPath(), "static-nodes.json");
        if (Files.exists(staticnodespath)) {
            return;
        }
        if (!Files.exists(staticnodespath.getParent())) {
            staticnodespath.getParent().toFile().mkdirs();
        }

        String localnodeaddress = getLocalEthereumAddress();
        try (FileWriter writer = new FileWriter(staticnodespath.toFile())) {
            writer.write("[\n");
            writer.write(
                "\"" + createEnodeURL(localnodeaddress, gethConfig.getGethNodePort(),
                    gethConfig.getRaftPort())
                    + "\"\n");
            writer.write("]\n");
        } catch (IOException e) {
            String message =
                "unable to generate static-nodes.json at " + staticnodespath.getParent();
            LOG.error(message);
            throw new APIException(message, e);
        }

        LOG.info("created static-nodes.json at " + staticnodespath.getParent());
    }

    public static String createEnodeURL(String localaddress, String gethport, String raftport) {
        String enodeurl = "enode://" + localaddress + "@127.0.0.1:" + gethport;

        if (raftport != null && raftport.trim().length() > 0
            && Integer.parseInt(raftport) > 0) {
            enodeurl += "?raftport=" + raftport;
        }

        return enodeurl;
    }

    public ArrayList<String> GethCommandLine() {
        return GethCommandLine(gethConfig.getConsensusMode());
    }

    /**
     * @param mode - raft, instanbul
     */
    public ArrayList<String> GethCommandLine(String mode) {
        if (null == mode || mode.trim().isEmpty()) {
            return GethRaftCommandLine();
        } else if (mode.equalsIgnoreCase("istanbul")) { // TODO: Mode should be an enum
            return GethIstanbulCommandLine();
        } else {
            return GethRaftCommandLine();
        }
    }

    public ArrayList<String> GethRaftCommandLine() {
        ArrayList<String> command = new ArrayList<String>();

        command.add(getGethPath());
        command.add("--datadir");
        command.add(gethConfig.getGethDataDirPath());
        command.add("--nodiscover");
        command.add("--rpc");
        command.add("--rpcaddr");
        command.add("127.0.0.1");
        command.add("--rpcapi");
        command.add(getRpcApiList("raft"));
        command.add("--rpcport");
        command.add(gethConfig.getRpcPort());
        command.add("--port");
        command.add(gethConfig.getGethNodePort());
        command.add("--nat");
        command.add("none");
        command.add("--raft");
        command.add("--raftport");
        command.add(gethConfig.getRaftPort());
        command.add("--raftblocktime");
        command.add(gethConfig.getRaftBlockFrequency() + "");
        if (gethConfig.getStartupMode().contentEquals("join")
            && gethConfig.getRaftNetworkId().length() > 0) {
            command.add("--raftjoinexisting");
            command.add(gethConfig.getRaftNetworkId());
        }

        return command;
    }

    public ArrayList<String> GethIstanbulCommandLine() {
        ArrayList<String> command = new ArrayList<String>();

        command.add(getGethPath());
        command.add("--datadir");
        command.add(gethConfig.getGethDataDirPath());
        command.add("--nodiscover");
        command.add("--syncmode");
        command.add("full");
        command.add("--mine");
        command.add("--rpc");
        command.add("--rpcaddr");
        command.add("127.0.0.1");
        command.add("--rpcapi");
        command.add(getRpcApiList("istanbul"));
        command.add("--rpcport");
        command.add(gethConfig.getRpcPort());
        command.add("--port");
        command.add(gethConfig.getGethNodePort());

        return command;
    }

    private void updateIstanbulGenesis() throws IOException {
        String localnodeaddress = getLocalEthereumAddress();

        //TODO remove << ISTANBUL WRAPPER
        String baseResourcePath = System.getProperty("eth.bin.dir");
        if (StringUtils.isBlank(baseResourcePath)) {
            baseResourcePath = FileUtils.getClasspathName("bin");
        }

        Path istanbullocation = Paths
            .get(expandPath(baseResourcePath, "quorum/istanbul-tools/mac/istanbul"));

        File istanbulbinary = istanbullocation.toFile();
        if (!istanbulbinary.canExecute()) {
            istanbulbinary.setExecutable(true);
        }

        List<String> istanbulcommand = Lists
            .newArrayList(istanbullocation.toString(), "extra", "encode", "--validators",
                "0x" + localnodeaddress);
        ProcessBuilder builder = new ProcessBuilder(istanbulcommand);
        LOG.info("generating instanbul extradata as " + String.join(" ", builder.command()));
        Process process = builder.start();

        String extradata = ""; // TODO: make a library call
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            extradata = scanner.next();
        }

        if (process.isAlive()) {
            process.destroy();
        }
        //TODO ISTANBUL WRAPPER

        File instabulgenesisfile = Paths
            .get(Paths.get(expandPath(baseResourcePath, "genesis")).toString(),
                "istanbul_genesis_block.json").toFile();
        JSONObject instabulgenesis = new JSONObject(
            IOUtils.toString(new FileInputStream(instabulgenesisfile)));
        instabulgenesis.put("extraData", extradata);
        FileWriter fw = new FileWriter(instabulgenesisfile);
        fw.write(instabulgenesis.toString());
        fw.flush();
        fw.close();

        Files.copy(instabulgenesisfile.toPath(),
            Paths.get(Paths.get(gethConfig.getGethDataDirPath()).getParent().toString(),
                "genesis_block.json"),
            StandardCopyOption.REPLACE_EXISTING);
    }

    private void updateRaftGenesis() throws IOException {
        String baseResourcePath = System.getProperty("eth.bin.dir");
        if (StringUtils.isBlank(baseResourcePath)) {
            baseResourcePath = FileUtils.getClasspathName("bin");
        }

        File raftgenesisfile = Paths
            .get(Paths.get(expandPath(baseResourcePath, "genesis")).toString(),
                "raft_genesis_block.json").toFile();
        Files.copy(raftgenesisfile.toPath(),
            Paths.get(Paths.get(gethConfig.getGethDataDirPath()).getParent().toString(),
                "genesis_block.json"),
            StandardCopyOption.REPLACE_EXISTING);
    }
}
