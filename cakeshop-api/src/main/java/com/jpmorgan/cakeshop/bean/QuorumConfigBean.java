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
    private static final String CONSTELLATION_MAC_KEYGEN = "quorum/constellation/mac/constellation-enclave-keygen";
    private final String CONSTELLATION_URL = StringUtils.isNotBlank(System.getProperty("geth.constellaiton.url"))
            ? System.getProperty("geth.constellaiton.url") : "http://127.0.0.1:9000";

    private final String EMBEDDED_NODE = null != System.getProperty("geth.node") ? System.getProperty("geth.node") : null;

    private String quorumPath;
    private String constellationPath;
    private String keyGen;
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
            String[] fileNames = dir.list();
            if (fileNames.length >= 4) {
                for (String fileName : fileNames) {
                    if (fileName.endsWith(".key") || fileName.endsWith(".pub")) {
                        createKeys = false;
                        break;
                    }
                }
            }
        }

        if (createKeys) {
            //create keys
            ProcessBuilder pb = new ProcessBuilder(getKeyGen(), destination.concat(keyName));
            Process process = pb.start();
            try (Scanner scanner = new Scanner(process.getInputStream())) {
                boolean flag = scanner.hasNext();
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                    while (flag) {
                        String line = scanner.next();
                        if (line.isEmpty()) {
                            continue;
                        }
                        if (line.contains("[none]:")) {
                            writer.newLine();
                            writer.flush();
                            writer.newLine();
                            writer.flush();
                            flag = false;
                        }
                    }
                }
            }

            int ret = process.waitFor();
            if (ret != 0) {
                LOG.error("Failed to generate keys. Please make sure that berkeley db is installed properly. Version of berkeley db is 6.2.23");
            } else {
                //create archive keys
                pb = new ProcessBuilder(getKeyGen(), destination.concat(keyName.concat("a")));
                process = pb.start();
                try (Scanner scanner = new Scanner(process.getInputStream())) {
                    boolean flag = scanner.hasNext();
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                        while (flag) {
                            String line = scanner.next();
                            if (line.isEmpty()) {
                                continue;
                            }
                            if (line.contains("[none]:")) {
                                writer.write(" ");
                                writer.flush();
                                writer.newLine();
                                writer.flush();
                                flag = false;
                            }
                        }
                    }
                }

                ret = process.waitFor();
                if (ret != 0) {
                    LOG.error("Failed to generate keys. Please make sure that berkeley db is installed properly. Version of berkeley db is 6.2.23");
                }
            }

            if (process.isAlive()) {
                process.destroy();
            }
        }
    }

    public void createQuorumConfig(String keyName, final String destination) throws IOException {
        File confFile = new File(destination.concat(keyName.concat(".conf")));
        if (!confFile.exists()) {
            keyName = destination.concat(keyName);
            try (FileWriter writer = new FileWriter(confFile)) {
                String url = CONSTELLATION_URL.endsWith("/") ? CONSTELLATION_URL.replaceFirst("(.*)" + "/" + "$", "$1" + "") : CONSTELLATION_URL;
                String port = url.substring(url.lastIndexOf(":") + 1, url.length());
                writer.write("url = \"" + url + "/" + "\"");
                writer.write("\n");
                writer.write("port = " + port);
                writer.write("\n");
                writer.write("socketPath = \"" + keyName + ".ipc\"");
                writer.write("\n");
                writer.write("otherNodeUrls = []");
                writer.write("\n");
                writer.write("publicKeyPath = \"" + keyName + ".pub\"");
                writer.write("\n");
                writer.write("privateKeyPath = \"" + keyName + ".key\"");
                writer.write("\n");
                writer.write("archivalPublicKeyPath = \"" + keyName + "a" + ".pub\"");
                writer.write("\n");
                writer.write("archivalPrivateKeyPath = \"" + keyName + "a" + ".key\"");
                writer.write("\n");
                writer.write("storagePath = \"" + destination + "constellation\"");
                writer.flush();
            }
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
