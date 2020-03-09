package com.jpmorgan.cakeshop.bean;

import com.jpmorgan.cakeshop.util.ProcessUtils;

import java.util.Arrays;
import java.util.List;

import static com.jpmorgan.cakeshop.util.FileUtils.expandPath;

public class TransactionManager {

    public static final String TESSERA_JAR = "tessera/tessera-app-0.10.4-app.jar";
    public static final String CONSTELLATION_COMMAND =
        "constellation/" + ProcessUtils.getPlatformDirectory() + "/constellation-node";
    public static final String CONSTELLATION_KEYGEN_PARAMS_TEMPLATE = "--generatekeys=%s";
    public static final String TESSERA_KEYGEN_PARAMS_TEMPLATE = "-keygen -filename %s";
    public static final String CONSTELLATION_PARAMS = "%s.conf --tls=off";
    public static final String TESSERA_PARAMS = "-configfile %s.json";

    public enum Type {
        tessera("tessera", TESSERA_KEYGEN_PARAMS_TEMPLATE,
            TESSERA_PARAMS, TESSERA_JAR, ".json"),
        constellation("constellation",
            CONSTELLATION_KEYGEN_PARAMS_TEMPLATE, CONSTELLATION_PARAMS, CONSTELLATION_COMMAND,
            ".conf"),
        none("none", "", "", "", "");


        public static final String TRANSACTION_MANAGER_KEY_NAME = "tm";
        private final String transactionManager;
        private final String keygenTemplate;
        private final String runTemplate;
        public final String transactionManagerName;
        private final String configExtension;

        Type(String transactionManagerName,
                               String keygenTemplate, String runTemplate, String transactionManager,
                               String configExtension) {
            this.transactionManagerName = transactionManagerName;
            this.transactionManager = transactionManager;
            this.keygenTemplate = keygenTemplate;
            this.runTemplate = runTemplate;
            this.configExtension = configExtension;
        }

        public String getTransactionManager(String ethGethDir) {
            String command = expandPath(ethGethDir, transactionManager);
            return command;
        }

        public List<String> getKeyGenParams(String destination, String keyname) {
            return Arrays.asList(
                String.format(keygenTemplate, expandPath(destination, keyname)).split("\\s"));
        }

        public List<String> getParams(String privateConfigPath, String configName) {
            return Arrays.asList(
                String.format(runTemplate, expandPath(privateConfigPath, configName)).split("\\s"));
        }

        public String getConfigExtension() {
            return configExtension;
        }
    }
}
