package com.jpmorgan.cakeshop.bean;

import com.jpmorgan.cakeshop.util.FileUtils;
import com.jpmorgan.cakeshop.util.SortedProperties;
import com.jpmorgan.cakeshop.util.StringUtils;
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
import java.util.Properties;

@Component
public class GethConfig {

    private static final Logger LOG = LoggerFactory.getLogger(GethConfig.class);

    //geth.db.enabled
    public static final String GETH_DB_ENABLED = "cakeshop.database.vendor";

    // User-configurable settings
    public static final String CONTRACT_REGISTRY_ADDR = "contract.registry.addr";
    public static final String CAKESHOP_SELECTED_NODE = "cakeshop.selected_node";

    public static final String NODE_BINARY_NAME = "nodejs.binary";


    @Value("${config.path}")
    private String dataDirectory;

    @Autowired
    private Environment env;

    private Properties props;

    private String customSpringConfigPath;

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
    }

    public boolean isDbEnabled() {
        return StringUtils.isNotBlank(env.getProperty(GETH_DB_ENABLED)) || StringUtils
            .isNotBlank(System.getProperty(GETH_DB_ENABLED));
    }

    public void setDbEnabled(String vendor) {
        props.setProperty(GETH_DB_ENABLED, vendor);
    }

    public String getContractAddress() {
        return props.getProperty(CONTRACT_REGISTRY_ADDR);
    }

    public void setContractAddress(String addr) {
        props.setProperty(CONTRACT_REGISTRY_ADDR, addr);
    }

    public Long getSelectedNode() {
        return Long.valueOf(props.getProperty(CAKESHOP_SELECTED_NODE, "1"));
    }

    public void setSelectedNode(Long nodeId) {
        props.setProperty(CAKESHOP_SELECTED_NODE, String.valueOf(nodeId));
    }

    public String getCakeshopPort() {
        return props.getProperty("server.port", "8080");
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

    public String getNodeJsBinaryName() {
        return get(NODE_BINARY_NAME, "node");
    }
}
