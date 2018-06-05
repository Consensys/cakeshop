/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.bean;

import com.jpmorgan.cakeshop.util.FileUtils;
import static com.jpmorgan.cakeshop.util.FileUtils.expandPath;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QuorumConfigBean implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(QuorumConfigBean.class);

    private static final String QUORUM_LINUX_COMMAND = "quorum/linux/geth";
    private static final String QUORUM_MAC_COMMAND = "quorum/mac/geth";
    private static final String CONSTELLATION_LINUX_COMMAND = "quorum/constellation/linux/constellation-node";
    private static final String CONSTELLATION_MAC_COMMAND = "quorum/constellation/mac/constellation-node";
    private static final String CONSTELLATION_LINUX_KEYGEN = "quorum/constellation/linux/constellation-enclave-keygen";
    private static final String CONSTELLATION_MAC_KEYGEN = "quorum/constellation/mac/constellation-node";
    private static final String CONSTELLATION_MAC_KEYGEN_PARAMS = "--generatekeys=node";
    private final String CONSTELLATION_URL = StringUtils.isNotBlank(System.getProperty("geth.constellation.url"))
            ? System.getProperty("geth.constellation.url") : "http://127.0.0.1:9000";

    private final String EMBEDDED_NODE = null != System.getProperty("geth.node") ? System.getProperty("geth.node") : null;

    private String quorumPath;
    private String constellationPath;
    private String keyGen;
    private String keyGenParams;
    private String constellationConfig;

    @Value("${geth.bootnodes.list:\"\"}")
    private String bootNodes;
    @Value("${geth.bootnode.key:\"\"}")
    private String bootNodeKey;
    @Value("${geth.bootnode.address:\"\"}")
    private String bootNodeAddress;
    @Value("${geth.boot.node:false}")
    private Boolean isBootNode;

    /**
     * @return the quorumPath
     */
    public String getQuorumPath() {
        return quorumPath;
    }

    /**
     * @param quorumPath the quorumPath to set
     */
    private void setQuorumPath(String quorumPath) {
        this.quorumPath = quorumPath;
    }

    /**
     * @return the constallationPath
     */
    public String getConstellationPath() {
        return constellationPath;
    }

    /**
     * @param constellationPath the constallationPath to set
     */
    private void setConstellationPath(String constallationPath) {
        this.constellationPath = constallationPath;
    }

    /**
     * @return the keyGen
     */
    public String getKeyGen() {
        return keyGen;
    }

    /**
     * @param keyGen the keyGen to set
     */
    private void setKeyGen(String keyGen) {
        this.keyGen = keyGen;
    }

    /**
     * @return the keyGenParams
     */
    public String getKeyGenParams() {
        return keyGenParams;
    }

    public String getKeyGenParams(String destination) {
        return keyGenParams.concat(" --workdir=" + destination); // TODO: passing this fails keygen from war file
    }

    /**
     * @param keyGenParams Commandline arguments for the key generator executable
     */
    private void setKeyGenParams(String keyGenParams) {
        this.keyGenParams = keyGenParams;
    }

    public String getConstellationConfigPath() {
        return constellationConfig;
    }

    /**
     * @return the bootNode
     */
    public String getBootNodes() {
        return bootNodes;
    }

    /**
     * @return the bootNodeKey
     */
    public String getBootNodeKey() {
        return bootNodeKey;
    }

    /**
     * @return the bootNodeAddress
     */
    public String getBootNodeAddress() {
        return bootNodeAddress;
    }

    /**
     * @return the isBootNode
     */
    public Boolean isBootNode() {
        return isBootNode;
    }

    public void createKeys(final String keyName, final String destination) throws IOException, InterruptedException {
        constellationConfig = destination;
        File dir = new File(destination);
        Boolean createKeys = true;

        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(destination), keyName + ".{key,pub}")) {
                int found = 0;
                for (Path entry: stream) {
                    LOG.info("found key file: " + entry);
                    found++;
                    if (found == 2) { createKeys = false; break;}
                }
            }
        }

        if (createKeys) {
            //create keys
            ProcessBuilder pb = new ProcessBuilder(getKeyGen(), getKeyGenParams());
            LOG.info("keygen command: " +  String.join(" ", pb.command()));
            Process process = pb.start();
            SendReturnToProcess(process);
            int ret = process.waitFor();
            MoveKeyFiles(keyName, destination); // TODO: this shouldn't be necessary
            if (ret != 0) {
                LOG.error("Failed to generate keys with code " + ret);
            }

            if (process.isAlive()) {
                process.destroy();
            }
        }
    }

    private void SendReturnToProcess(Process process) throws IOException {
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            boolean flag = scanner.hasNext();
            while (flag) {
                String line = scanner.next(); // TODO: default delimiter is whitespace, so it reads by word, not line
                if (line.isEmpty()) {
                    continue;
                }

                if (line.contains("[none]:")) {
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                        writer.newLine();
                        writer.flush();
                        flag = false;
                    }
                }
            }
        }
    }

    private void MoveKeyFiles(final String keyName, final String destination) throws IOException {
        File dir = Paths.get(destination).toFile();
        File dpub = Paths.get(dir.getAbsolutePath(), keyName.concat(".pub")).toFile();
        File dkey = Paths.get(dir.getAbsolutePath(), keyName.concat(".key")).toFile();

        File location = Paths.get(System.getProperty("user.dir")).toFile();
        File pub = Paths.get(location.getAbsolutePath(), keyName.concat(".pub")).toFile();
        File key = Paths.get(location.getAbsolutePath(), keyName.concat(".key")).toFile();

        Files.move(pub.toPath(), dpub.toPath());
        Files.move(key.toPath(), dkey.toPath());
    }

    //TODO: convert to commandline
    public void createQuorumConfig(final String keyName, final String destination) throws IOException {
        File confFile = Paths.get(destination, keyName.concat(".conf")).toFile();
        if (!confFile.exists()) {
            String prefix = confFile.getParent() + File.separator + keyName;
            try (FileWriter writer = new FileWriter(confFile)) {
                String urlstring = CONSTELLATION_URL.endsWith("/") ? CONSTELLATION_URL.replaceFirst("(.*)" + "/" + "$", "$1" + "") : CONSTELLATION_URL;
                URL url = new URL(urlstring);

                writer.write("url = \"" + url + "\"");
                writer.write("\n");
                writer.write("port = " + url.getPort());
                writer.write("\n");
                writer.write("socket = \"" + prefix + ".ipc\"");
                writer.write("\n");
                writer.write("othernodes = []");
                writer.write("\n");
                writer.write("publickeys = [\"" + prefix + ".pub\"]");
                writer.write("\n");
                writer.write("privatekeys = [\"" + prefix + ".key\"]");
                writer.write("\n");
                writer.write("storage = \"dir:" + confFile.getParent() + File.separator + "constellation\"");
                writer.flush();
                LOG.info("created constellation config at " + confFile.getPath());
            }
        } else {
            LOG.info("reusing constellation config at " + confFile.getPath());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initQuorumBean();
    }

    private void initQuorumBean() {

        // setup needed paths
        String baseResourcePath = System.getProperty("eth.geth.dir");
        if (StringUtils.isBlank(baseResourcePath)) {
            baseResourcePath = FileUtils.getClasspathName("geth");
        }

        if (SystemUtils.IS_OS_LINUX) {
            LOG.debug("Using quorum for linux");
            setQuorumPath(expandPath(baseResourcePath, QUORUM_LINUX_COMMAND));
            setConstellationPath(expandPath(baseResourcePath, CONSTELLATION_LINUX_COMMAND));
            setKeyGen(expandPath(baseResourcePath, CONSTELLATION_LINUX_KEYGEN));

        } else if (SystemUtils.IS_OS_MAC_OSX) {
            LOG.debug("Using quorum for mac");
            setQuorumPath(expandPath(baseResourcePath, QUORUM_MAC_COMMAND));
            setConstellationPath(expandPath(baseResourcePath, CONSTELLATION_MAC_COMMAND));
            setKeyGen(expandPath(baseResourcePath, CONSTELLATION_MAC_KEYGEN));
            setKeyGenParams(CONSTELLATION_MAC_KEYGEN_PARAMS);

        } else if ( (SystemUtils.IS_OS_WINDOWS) && (StringUtils.equalsIgnoreCase(EMBEDDED_NODE, "geth")) ) {
            // run GETH
            return;

        } else if (SystemUtils.IS_OS_WINDOWS) {
            LOG.error("Running on unsupported OS! Only Linux and Mac OS X are currently supported for Quorum, on Windoze, please run with -Dgeth.node=geth");
            throw new IllegalArgumentException("Running on unsupported OS! Only Linux and Mac OS X are currently supported for Quorum, on Windoze, please run with -Dgeth.node=geth");

        } else {
            LOG.error("Running on unsupported OS! Only Linux and Mac OS X are currently supported");
            throw new IllegalArgumentException("Running on unsupported OS! Only Linux and Mac OS X are currently supported");
        }

        File quorumExec = new File(getQuorumPath());
        if (!quorumExec.canExecute()) {
            quorumExec.setExecutable(true);
        }

        File constExec = new File(getConstellationPath());
        if (!constExec.canExecute()) {
            constExec.setExecutable(true);
        }

        File keyGenExec = new File(getKeyGen());
        if (!keyGenExec.canExecute()) {
            keyGenExec.setExecutable(true);
        }

    }

}
