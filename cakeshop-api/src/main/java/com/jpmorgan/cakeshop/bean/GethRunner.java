package com.jpmorgan.cakeshop.bean;

import static com.jpmorgan.cakeshop.util.FileUtils.expandPath;
import static com.jpmorgan.cakeshop.util.ProcessUtils.ensureFileIsExecutable;

import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.ProcessUtils;
import com.jpmorgan.cakeshop.util.StringUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
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

    private String gethPasswordFile;

    private String genesisBlockFilename;

    private String keystorePath;

    private String nodeJsPath;

    private String solcPath;

    /**
     * Whether or not this is a quorum node
     */
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

        // init genesis block file (using vendor copy if necessary)
        String vendorGenesisDir = expandPath(binPath,
            "genesis"); // TODO: this block is redundant now
        genesisBlockFilename = expandPath(gethConfig.getDataDirectory(), "genesis_block.json");

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

        gethConfig.setGethDataDirPath(expandPath(gethConfig.getDataDirectory(), "ethereum"));

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
            setIsEmbeddedQuorum(true);
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
        return expandPath(gethConfig.getDataDirectory(), "geth.pid");
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
            .get(expandPath(baseResourcePath,
                "quorum/istanbul-tools/" + ProcessUtils.getPlatformDirectory() + "/istanbul"));

        File istanbulbinary = istanbullocation.toFile();
        if (!istanbulbinary.canExecute()) {
            istanbulbinary.setExecutable(true);
        }

        List<String> istanbulcommand = Lists
            .newArrayList(istanbullocation.toString(),
                "reinit",
                "--nodekey",
                FileUtils.readFileToString(verifyNodeKey().toFile(), Charset.defaultCharset()));
        if (gethConfig.shouldUseQuorum()) {
            istanbulcommand.add("--quorum");
        }
        ProcessBuilder builder = new ProcessBuilder(istanbulcommand);
        LOG.info(
            "generating instanbul genesis_block.json as " + String.join(" ", builder.command()));
        Process process = builder.start();

        String jsonOutput = IOUtils.toString(process.getInputStream(), Charset.defaultCharset());

        if (process.isAlive()) {
            process.destroy();
        }

        FileWriter fw = new FileWriter(
            Paths.get(gethConfig.getDataDirectory(), "genesis_block.json").toFile());
        fw.write(jsonOutput);
        fw.flush();
        fw.close();
    }

    private void updateRaftGenesis() throws IOException {
        String baseResourcePath = System.getProperty("eth.bin.dir");
        if (StringUtils.isBlank(baseResourcePath)) {
            baseResourcePath = FileUtils.getClasspathName("bin");
        }

        File raftgenesisfile = Paths
            .get(Paths.get(expandPath(baseResourcePath, "genesis")).toString(),
                "genesis_block.json").toFile();
        Files.copy(raftgenesisfile.toPath(),
            Paths.get(Paths.get(gethConfig.getGethDataDirPath()).getParent().toString(),
                "genesis_block.json"),
            StandardCopyOption.REPLACE_EXISTING);
    }
}
