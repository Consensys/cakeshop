package com.jpmorgan.cakeshop.bean;

import static com.jpmorgan.cakeshop.service.impl.NodeServiceImpl.STATIC_NODES_JSON;
import static com.jpmorgan.cakeshop.util.FileUtils.expandPath;
import static com.jpmorgan.cakeshop.util.ProcessUtils.ensureFileIsExecutable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.error.APIException;
import com.jpmorgan.cakeshop.model.Genesis;
import com.jpmorgan.cakeshop.util.DownloadUtils;
import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GethRunner {

    private static final Logger LOG = LoggerFactory.getLogger(GethRunner.class);

    private RestTemplate restTemplate;

    private final GethConfig gethConfig;

    private final ObjectMapper jsonMapper;

    private String gethPasswordFile;

    private String genesisBlockFilename;

    private String keystorePath;

    /**
     * Whether or not this is a quorum node
     */
    private Boolean isEmbeddedQuorum;

    private String binPath;
    private String enodeUrl;

    @Autowired
    public GethRunner(GethConfig gethConfig, ObjectMapper jsonMapper, RestTemplate restTemplate) {
        this.gethConfig = gethConfig;
        this.jsonMapper = jsonMapper;
        this.restTemplate = restTemplate;
    }

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

    }
    public void initializeConsensusMode() throws IOException {
        addToEnodesConfig(getEnodeURL(), STATIC_NODES_JSON);
        if (gethConfig.getConsensusMode().equalsIgnoreCase("istanbul")) {
            updateIstanbulGenesis();
        } else if (gethConfig.getConsensusMode().equalsIgnoreCase("raft")) {
            updateRaftGenesis();
        }
    }

    public void reset() throws IOException {
        enodeUrl = null;
        FileUtils.deleteDirectory(new File(gethConfig.getGethDataDirPath()));
    }

    public void clearRaftStateIfSingleNode() throws IOException {
        if (getCurrentEnodesList(STATIC_NODES_JSON).size() <= 1) {
            LOG.info(
                "Single node found in static-nodes.json, deleting any existing raft folders to fix a leader election bug in raft");
            String ethereumFolder = gethConfig.getGethDataDirPath();
            FileUtils.deleteQuietly(new File(ethereumFolder, "quorum-raft-state"));
            FileUtils.deleteQuietly(new File(ethereumFolder, "raft-snap"));
            FileUtils.deleteQuietly(new File(ethereumFolder, "raft-wal"));
        }

    }

    public String getGethPath() {
        return expandPath(binPath, "quorum/geth");
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
        return expandPath(binPath, "solc", "node_modules", "solc-cakeshop-cli", "bin", "solc");
    }

    public String getGenesisBlock() throws IOException {
        return FileUtils.readFileToString(new File(genesisBlockFilename));
    }

    public void setGenesisBlock(String genesisBlock) throws IOException {
        FileUtils.writeStringToFile(new File(genesisBlockFilename), genesisBlock);
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

    public void addToEnodesConfig(String enodeURL, String fileName) throws IOException {
        List<String> enodeIds = getCurrentEnodesList(fileName);

        if (enodeIds.contains(enodeURL)) {
            LOG.info("static-nodes.json already includes enode url");
            return;
        }

        enodeIds.add(enodeURL);

        writeEnodesList(enodeIds, fileName);
    }

    public void removeFromEnodesConfig(String enodeURL, String fileName) throws IOException {
        List<String> enodeIds = getCurrentEnodesList(fileName);

        if (!enodeIds.contains(enodeURL)) {
            LOG.info("static-nodes.json doesn't include enode url");
            return;
        }

        enodeIds.remove(enodeURL);

        writeEnodesList(enodeIds, fileName);
    }

    private List<String> getCurrentEnodesList(String fileName) throws IOException {
        Path enodeListFilePath = Paths.get(gethConfig.getGethDataDirPath(), fileName);
        List<String> enodeIds;
        if (Files.exists(enodeListFilePath)) {
            enodeIds = jsonMapper.readValue(
                enodeListFilePath.toFile(),
                new TypeReference<List<String>>() {
                });
        } else {
            enodeListFilePath.getParent().toFile().mkdirs();
            enodeIds = new ArrayList<>();
        }
        return enodeIds;
    }

    private void writeEnodesList(List<String> enodeIds, String fileName) throws APIException {
        Path enodeListFilePath = Paths.get(gethConfig.getGethDataDirPath(), fileName);
        try {
            enodeListFilePath.getParent().toFile().mkdirs();
            jsonMapper.writeValue(enodeListFilePath.toFile(), enodeIds);
        } catch (IOException e) {
            String message =
                "unable to generate static-nodes.json at " + enodeListFilePath.getParent();
            LOG.error(message);
            throw new APIException(message, e);
        }

        LOG.info("updated static-nodes.json at " + enodeListFilePath.getParent());
    }

    public String getEnodeURL() throws IOException {
        if(enodeUrl == null) {
            enodeUrl = formatEnodeUrl(getLocalEthereumAddress(), "127.0.0.1",
                gethConfig.getGethNodePort(),
                gethConfig.getRaftPort());
        }
        return enodeUrl;
    }

    public String formatEnodeUrl(String address, String ip, String port, String raftPort) {
        String enodeurl = String.format("enode://%s@%s:%s", address, ip, port);

        if (raftPort != null && raftPort.trim().length() > 0 && Integer.parseInt(raftPort) > 0) {
            enodeurl += "?raftport=" + raftPort;
        }

        return enodeurl;
    }

    public ArrayList<String> gethCommandLine() {
        return gethCommandLine(gethConfig.getConsensusMode());
    }

    /**
     * @param mode - raft, instanbul
     */
    public ArrayList<String> gethCommandLine(String mode) {
        if (null == mode || mode.trim().isEmpty()) {
            return gethRaftCommandLine();
        } else if (mode.equalsIgnoreCase("istanbul")) { // TODO: Mode should be an enum
            return gethIstanbulCommandLine();
        } else {
            return gethRaftCommandLine();
        }
    }

    public ArrayList<String> gethRaftCommandLine() {
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

    public ArrayList<String> gethIstanbulCommandLine() {
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
        String baseResourcePath = getBaseResourcePath();

        Path istanbulLocation = Paths.get(expandPath(baseResourcePath, "quorum/istanbul"));

        List<String> istanbulCommand = Lists
            .newArrayList(istanbulLocation.toString(),
                "reinit",
                "--nodekey",
                FileUtils.readFileToString(verifyNodeKey().toFile(), Charset.defaultCharset()));
        if (gethConfig.shouldUseQuorum()) {
            istanbulCommand.add("--quorum");
        }
        ProcessBuilder builder = new ProcessBuilder(istanbulCommand);
        LOG.info(
            "generating instanbul genesis_block.json as " + String.join(" ", builder.command()));
        Process process = builder.start();

        Genesis istanbulGenesis = jsonMapper.readValue(process.getInputStream(), Genesis.class);

        if (process.isAlive()) {
            process.destroy();
        }

        jsonMapper.writeValue(
            Paths.get(gethConfig.getDataDirectory(), "genesis_block.json").toFile(),
            mergeWithBaseGenesis(istanbulGenesis));
    }

    private String getBaseResourcePath() {
        String baseResourcePath = System.getProperty("eth.bin.dir");
        if (StringUtils.isBlank(baseResourcePath)) {
            baseResourcePath = FileUtils.getClasspathName("bin");
        }
        return baseResourcePath;
    }

    private Genesis getBaseGenesis() throws IOException {
        String baseResourcePath = getBaseResourcePath();
        File baseGenesisFile = Paths
            .get(Paths.get(expandPath(baseResourcePath, "genesis")).toString(),
                "genesis_block.json").toFile();
        return jsonMapper.readValue(baseGenesisFile, Genesis.class);
    }

    private Genesis mergeWithBaseGenesis(Genesis istanbulGenesis) throws IOException {
        // the istanbul-tools generated json doesn't include the right config settings or the
        // allocations to our ethereum accounts. Merge them together, overwriting the base with
        // values from the istanbul genesis file where necessary
        Genesis baseGenesis = getBaseGenesis();

        baseGenesis.alloc.putAll(istanbulGenesis.alloc);
        baseGenesis.config.istanbul = istanbulGenesis.config.istanbul;
        baseGenesis.config.isQuorum = istanbulGenesis.config.isQuorum;
        baseGenesis.extraData = istanbulGenesis.extraData;
        baseGenesis.mixhash = istanbulGenesis.mixhash;
        baseGenesis.timestamp = istanbulGenesis.timestamp;
        baseGenesis.difficulty = istanbulGenesis.difficulty;
        baseGenesis.number = istanbulGenesis.number;
        baseGenesis.gasUsed = istanbulGenesis.gasUsed;
        baseGenesis.parentHash = istanbulGenesis.parentHash;
        return baseGenesis;
    }

    private void updateRaftGenesis() throws IOException {
        Genesis baseGenesis = getBaseGenesis();
        jsonMapper
            .writeValue(Paths.get(gethConfig.getDataDirectory(), "genesis_block.json").toFile(),
                baseGenesis);
    }

    public void downloadQuorumIfNeeded() {
        File gethDirectory = new File(getGethPath()).getParentFile();
        if (!gethDirectory.exists()) {
            LOG.info("Quorum binary doesn't exist, creating bin directory");
            gethDirectory.mkdirs();
        }

        File quorum = new File(gethDirectory, "geth");
        if (!quorum.exists()) {
            LOG.info("Downloading quorum from: {}", gethConfig.getGethReleaseUrl());
            restTemplate.execute(URI.create(gethConfig.getGethReleaseUrl()), HttpMethod.GET, DownloadUtils.getOctetStreamRequestCallback(), DownloadUtils.createTarResponseExtractor(quorum.getAbsolutePath(), "geth"));
            LOG.info("Done downloading quorum");
        }

        File bootnode = new File(gethDirectory, "bootnode");
        if (!bootnode.exists()) {
            LOG.info("Downloading bootnode from: {}", gethConfig.getGethToolsUrl());
            restTemplate.execute(URI.create(gethConfig.getGethToolsUrl()), HttpMethod.GET, DownloadUtils.getOctetStreamRequestCallback(), DownloadUtils.createTarResponseExtractor(bootnode.getAbsolutePath(), "bootnode"));
            LOG.info("Done downloading bootnode");
        }

        File istanbulTools = new File(gethDirectory, "istanbul");
        if (!istanbulTools.exists()) {
            LOG.info("Downloading istanbul-tools from: {}", gethConfig.getIstanbulToolsUrl());
            restTemplate.execute(URI.create(gethConfig.getIstanbulToolsUrl()), HttpMethod.GET, DownloadUtils.getOctetStreamRequestCallback(), DownloadUtils.createTarResponseExtractor(istanbulTools.getAbsolutePath(), "istanbul"));
            LOG.info("Done downloading istanbul-tools");
        }
    }
}
