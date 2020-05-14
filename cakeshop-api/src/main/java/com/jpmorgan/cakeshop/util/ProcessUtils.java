package com.jpmorgan.cakeshop.util;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtils.class);

    public static ProcessBuilder createProcessBuilder(List<String> commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);

        if (LOG.isDebugEnabled()) {
            LOG.debug(Joiner.on(" ").join(builder.command()));
        }

        return builder;
    }

    /**
     * Check if the given PID is running (supports both Unix and Windows
     * systems)
     *
     * @param pid
     * @return
     */
    public static boolean isProcessRunning(String pid) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return isProcessRunningWin(pid);
        } else {
            return isProcessRunningNix(pid);
        }
    }

    public static boolean isProcessRunningNix(String pid) {
        if (StringUtils.isEmpty(pid)) {
            return false;
        }

        try {
            Process exec = Runtime.getRuntime().exec("kill -0 " + pid);
            exec.waitFor();
            return (exec.exitValue() == 0); // process is running when kill -0 returns 0 (signal 0 was successfully sent)

        } catch (IOException | InterruptedException ex) {
            LOG.error(ex.getMessage());
        }

        return false;
    }

    public static boolean isProcessRunningWin(String pid) {
        try {
            Process proc = Runtime.getRuntime().exec(new String[]{"cmd", "/c", "tasklist /FI \"PID eq " + pid + "\""});
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                //Parsing the input stream.
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(" " + pid + " ")) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return false;
    }

    public static boolean killProcess(String pidFileName, String name)
        throws InterruptedException, IOException {
        String pid = readPidFromFile(pidFileName);
        LOG.info("Stopping {} process with pid {}", name, pid);
        boolean killed = SystemUtils.IS_OS_WINDOWS ? killProcessWin(pid) : killProcessNix(pid);
        if (!killed) {
            LOG.warn("Failed to kill process with pid {}", pid);
            return false;
        }

        if (!new File(pidFileName).delete()) {
            LOG.warn("Could not delete pid file {}", pidFileName);
        }

        // wait for process to actually stop
        while (true) {
            if (!isProcessRunning(pid)) {
                return true;
            }
            LOG.info("Process with pid {} hasn't stopped yet, waiting", pid);
            TimeUnit.MILLISECONDS.sleep(5);
        }
    }

    public static boolean killProcessWin(String pid) throws InterruptedException, IOException {
        Runtime.getRuntime().exec("taskkill /F /PID " + pid).waitFor();
        return true;
    }

    public static boolean killProcessNix(String pid) throws InterruptedException, IOException {
        Runtime.getRuntime().exec("kill " + pid).waitFor();
        return true;
    }

    public static String readPidFromFile(String pidFilename) {
        File pidFile = new File(pidFilename);
        if (!pidFile.exists()) {
            return null;
        }
        String pid = null;
        try {
            pid = FileUtils.readFileToString(pidFile);
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return pid;
    }

    public static void writePidToFile(Long pid, String pidFilename) throws IOException {
        LOG.info("Creating pid file: " + pidFilename);
        File pidFile = new File(pidFilename);
        if (!pidFile.exists()) {
            pidFile.createNewFile();
        }
        try (FileWriter writer = new FileWriter(pidFile)) {
            writer.write(String.valueOf(pid));
            writer.flush();
        }
    }

    /**
     * Ensure that given file, if it exists, is executable
     *
     * @param filename
     */
    public static boolean ensureFileIsExecutable(String filename) {
        File file = new File(filename);
        LOG.info("testing {} exists: {}", filename, file.exists());
        if (file.exists()) {
            if (file.canExecute()) {
                return true;
            }
            return file.setExecutable(true);
        }
        return false;
    }

    public static String getPlatformDirectory() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "win";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "linux";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            return "mac";
        } else {
            LOG.error(
                "Running on unsupported OS! Only Windows, Linux and Mac OS X are currently supported");
            throw new IllegalArgumentException(
                "Running on unsupported OS! Only Windows, Linux and Mac OS X are currently supported");
        }
    }
}
