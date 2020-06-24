package com.jpmorgan.cakeshop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.jpmorgan.cakeshop.util.FileUtils.expandPath;

public class CakeshopUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CakeshopUtils.class);

    public static String formatEnodeUrl(String address, String ip, String port, String raftPort) {
        String enodeurl = String.format("enode://%s@%s:%s", address, ip, port);

        if (raftPort != null && raftPort.trim().length() > 0 && Integer.parseInt(raftPort) > 0) {
            enodeurl += "?raftport=" + raftPort;
        }

        return enodeurl;
    }

    public static String getSolcPath() {
        // setup needed paths
        String binPath = System.getProperty("eth.bin.dir");
        if (StringUtils.isBlank(binPath)) {
            binPath = FileUtils.getClasspathName("bin");
        }
        return expandPath(binPath, "solc", "node_modules", "solc-cakeshop-cli", "bin", "solc");
    }
}
