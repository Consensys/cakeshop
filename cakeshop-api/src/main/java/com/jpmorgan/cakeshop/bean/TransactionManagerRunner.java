/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpmorgan.cakeshop.bean;

import static com.jpmorgan.cakeshop.util.FileUtils.expandPath;
import static com.jpmorgan.cakeshop.util.ProcessUtils.getProcessPid;
import static com.jpmorgan.cakeshop.util.ProcessUtils.writePidToFile;

import com.jpmorgan.cakeshop.util.FileUtils;
import com.moandjiezana.toml.TomlWriter;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.config.util.jaxb.MarshallerBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
public class TransactionManagerRunner implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionManagerRunner.class);

    public GethConfig gethConfig;

    private String ethGethDir;

    @Autowired
    public TransactionManagerRunner(GethConfig gethConfig) {
        this.gethConfig = gethConfig;
    }

    public void createTransactionManagerNodeKeys() throws IOException, InterruptedException {

        if (!keysExist(TransactionManager.Type.TRANSACTION_MANAGER_KEY_NAME)) {
            //create keys
            List<String> commandArgs = generateMainCommand(
                gethConfig.getTransactionManagerType().getTransactionManager(ethGethDir));
            commandArgs.addAll(gethConfig.getTransactionManagerType()
                .getKeyGenParams(gethConfig.getTransactionManagerDataPath(),
                    TransactionManager.Type.TRANSACTION_MANAGER_KEY_NAME));
            ProcessBuilder pb = new ProcessBuilder(commandArgs);

            LOG.info("keygen command: " + String.join(" ", pb.command()));
            pb.redirectErrorStream(true); // redirect error stream to output stream
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            // autofill empty password on key generation prompts
            pb.redirectInput(Redirect.from(new File("/dev/null")));
            Process process = pb.start();
            SendReturnToProcess(process);
            int ret = process.waitFor();
            if (ret != 0) {
                LOG.error("Failed to generate keys with code " + ret + " " + StreamUtils
                    .copyToString(process.getErrorStream(), Charset.defaultCharset()));
            }

            if (process.isAlive()) {
                process.destroy();
            }
        }
    }

    private List<String> generateMainCommand(String executable) {
        List<String> commandArgs = new ArrayList<>();
        if (executable.endsWith(".jar")) {
            commandArgs.add("java");
            commandArgs.add("-jar");
        }
        commandArgs.add(executable);
        return commandArgs;
    }

    private boolean keysExist(String keyName) throws IOException {
        File dir = new File(gethConfig.getTransactionManagerDataPath());
        boolean keysExist = false;

        if (!dir.exists()) {
            dir.mkdirs();
        } else {
            try (DirectoryStream<Path> stream = Files
                .newDirectoryStream(Paths.get(gethConfig.getTransactionManagerDataPath()),
                    keyName + ".{key,pub}")) {
                int found = 0;
                for (Path entry : stream) {
                    LOG.info("found key file: " + entry);
                    found++;
                    if (found == 2) {
                        keysExist = true;
                        break;
                    }
                }
            }
        }
        return keysExist;
    }


    static void SendReturnToProcess(Process process) throws IOException {
        try (Scanner scanner = new Scanner(process.getInputStream())) {
            boolean flag = scanner.hasNext();
            while (flag) {
                String line = scanner
                    .next(); // TODO: default delimiter is whitespace, so it reads by word, not line
                if (line.isEmpty()) {
                    continue;
                }

                if (line.contains("[none]:")) {
                    try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream()))) {
                        writer.newLine();
                        writer.flush();
                        flag = false;
                    }
                }
            }
        }
    }

    public void writeTransactionManagerConfig()
        throws IOException {
        LOG.info("Transaction manager transactionManagerType: {}",
            gethConfig.getTransactionManagerType());
        switch (gethConfig.getTransactionManagerType()) {
            case constellation:
                writeConstellationConfig(gethConfig.getTesseraConfig());
                break;
            case none:
                break;
            case tessera:
            default:
                writeTesseraConfig(gethConfig.getTesseraConfig());
                break;
        }
    }

    public String getPidFilePath() {
        return expandPath(gethConfig.getTransactionManagerDataPath(),
            gethConfig.getTransactionManagerType().transactionManagerName + ".pid");
    }

    public void writeConstellationConfig(Config config) throws IOException {
        File confFile = getTransactionManagerConfigPath().toFile();
        try (FileWriter writer = new FileWriter(confFile)) {
            // remove trailing slash
            Properties properties = new Properties();
            properties.put("url", config.getServer().getHostName());
            properties.put("port", config.getServer().getPort());
            properties.put("socket", config.getUnixSocketFile());
            properties.put("othernodes", config.getPeers());
            properties.put("publickeys",
                config.getKeys().getKeyData().stream().map(ConfigKeyPair::getPublicKey).toArray());
            properties.put("privatekeys",
                config.getKeys().getKeyData().stream().map(ConfigKeyPair::getPrivateKey).toArray());

            final Path storage = Paths.get(gethConfig.getTransactionManagerDataPath());
            properties
                .put("storage", "dir:" + storage.toString() + File.separator + "constellation");

            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(properties, writer);
            LOG.info("created constellation config at " + confFile.getPath());
        }
    }

    public Path getTransactionManagerConfigPath() {
        return Paths.get(gethConfig.getTransactionManagerDataPath(),
            TransactionManager.Type.TRANSACTION_MANAGER_KEY_NAME.concat(
                gethConfig.getTransactionManagerType().getConfigExtension()));
    }

    public void writeTesseraConfig(Config config) {
        File confFile = getTransactionManagerConfigPath().toFile();
        try (FileOutputStream fileOutputStream = new FileOutputStream(confFile)) {
            System.setProperty("javax.xml.bind.context.factory",
                "org.eclipse.persistence.jaxb.JAXBContextFactory");
            MarshallerBuilder.create().build().marshal(config, fileOutputStream);
            fileOutputStream.flush();
            LOG.info("created tessera config at " + confFile.getPath());
        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }

    }

    public boolean startTransactionManager() {
        File logFile;
        try {
            File logDir = new File(gethConfig.getTransactionManagerDataPath().concat("logs"));
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            logFile = new File(logDir,
                TransactionManager.Type.TRANSACTION_MANAGER_KEY_NAME + ".log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
        } catch (IOException ex) {
            LOG.error("Could not create log for transaction manager", ex.getMessage());
            return false;
        }

        try {
            if (gethConfig.getTransactionManagerType() != TransactionManager.Type.none) {
                createTransactionManagerNodeKeys();
                writeTransactionManagerConfig();
            }

            List<String> commandArgs = generateMainCommand(
                gethConfig.getTransactionManagerType().getTransactionManager(ethGethDir));
            commandArgs.addAll(
                gethConfig.getTransactionManagerType().getParams(
                    gethConfig.getTransactionManagerDataPath(),
                    TransactionManager.Type.TRANSACTION_MANAGER_KEY_NAME));
            ProcessBuilder builder = new ProcessBuilder(commandArgs);
            builder.redirectOutput(logFile);
            builder.redirectErrorStream(true); // redirect error stream to output stream
            builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process process = builder.start();
            Integer constProcessId = getProcessPid(process);
            LOG.info("Transaction Manager started as " + String.join(" ", builder.command()));
            TimeUnit.SECONDS.sleep(5);
            if (constProcessId == -1) {
                throw new RuntimeException("Error starting transaction manager");
            } else {
                writePidToFile(constProcessId, getPidFilePath());
                return true;
            }
        } catch (IOException | InterruptedException ex) {
            LOG.error("Error starting transaction manager", ex);
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initQuorumBean();
    }

    private void initQuorumBean() {

        // setup needed paths
        ethGethDir = System.getProperty("eth.bin.dir");

        if (StringUtils.isBlank(ethGethDir)) {
            ethGethDir = FileUtils.getClasspathName("bin");
        }

        File constExec = new File(
            gethConfig.getTransactionManagerType().getTransactionManager(ethGethDir));
        if (!constExec.canExecute()) {
            constExec.setExecutable(true);
        }
    }
}
