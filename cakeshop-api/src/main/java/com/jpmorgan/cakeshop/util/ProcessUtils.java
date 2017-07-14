package com.jpmorgan.cakeshop.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.jpmorgan.cakeshop.bean.GethConfigBean;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessUtils {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessUtils.class);

    public static ProcessBuilder createProcessBuilder(GethConfigBean gethConfig, String... commands) {
        return createProcessBuilder(gethConfig, Lists.newArrayList(commands));
    }

    public static ProcessBuilder createProcessBuilder(GethConfigBean gethConfig, List<String> commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);

        // need to modify PATH so it can locate compilers correctly
        String solcDir = new File(gethConfig.getSolcPath()).getParent();
        final Map<String, String> env = builder.environment();
        env.put("PATH", prefixPathStr(gethConfig.getBinPath() + File.pathSeparator + solcDir, env.get("PATH")));

        if (LOG.isDebugEnabled()) {
            LOG.debug(Joiner.on(" ").join(builder.command()));
        }

        // libgmp is no longer used, but keep this in place anyway, just in case
        // we need to add some other dynamic libs later
        if (SystemUtils.IS_OS_MAC_OSX) {
            // we ship the gmp lib at this location, make sure its accessible
            env.put("DYLD_LIBRARY_PATH", prefixPathStr(gethConfig.getBinPath(), env.get("DYLD_LIBRARY_PATH")));
        } else if (SystemUtils.IS_OS_LINUX) {
            env.put("LD_LIBRARY_PATH", prefixPathStr(gethConfig.getBinPath(), env.get("LD_LIBRARY_PATH")));
        }

        return builder;
    }

    private static String prefixPathStr(String newPath, String currPath) {
        if (currPath != null && !currPath.trim().isEmpty()) {
            newPath = newPath + File.pathSeparator + currPath.trim();
        }
        return newPath;
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

    public static boolean killProcess(String pid, String exeName) throws InterruptedException, IOException {
        boolean killed = SystemUtils.IS_OS_WINDOWS ? killProcessWin(pid) : killProcessNix(pid);
        if (!killed) {
            return false;
        }

        // wait for process to actually stop
        while (true) {
            if (!isProcessRunning(pid)) {
                return true;
            }
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

    public static Integer getProcessPid(Process process) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return getWinPID(process);
        }
        return getUnixPID(process);
    }

    public static Integer getUnixPID(Process process) {
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            try {
                Class<? extends Process> cl = process.getClass();
                Field field = cl.getDeclaredField("pid");
                field.setAccessible(true);
                Object pidObject = field.get(process);
                return (Integer) pidObject;

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                LOG.error("Cannot get UNIX pid: " + ex.getMessage());
            }
        }

        return null;
    }

    public static String getUnixPidByName(String processName) {
        String[] command = new String[]{"/bin/sh", "-c",
            " ps -ef | grep ".concat(processName)};
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            Process process = builder.start();
            try (InputStream input = process.getInputStream()) {
                byte[] b = new byte[16];
                input.read(b, 0, b.length);
                String[] commandLineresult = new String(b).split("\\s+");
                if (SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC) {
                    return commandLineresult[2];
                } else if (SystemUtils.IS_OS_LINUX || SystemUtils.IS_OS_UNIX) {
                    return commandLineresult[1];
                }
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    //TODO: Test on Windows
    public static String getWinPidByName(String processName) {
        String[] command = new String[]{"TASKLIST /FI \"USERNAME ne NT AUTHORITY\\SYSTEM\" | findstr ".concat(processName)};
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            Process process = builder.start();
            try (InputStream input = process.getInputStream()) {
                byte[] b = new byte[16];
                input.read(b, 0, b.length);
                return new String(b).split("\\s+")[1];
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return null;
    }

    public static Integer getWinPID(Process proc) {
        if (proc.getClass().getName().equals("java.lang.Win32Process")
                || proc.getClass().getName().equals("java.lang.ProcessImpl")) {

            try {
                Field f = proc.getClass().getDeclaredField("handle");
                f.setAccessible(true);
                long handl = f.getLong(proc);
                Kernel32 kernel = Kernel32.INSTANCE;
                WinNT.HANDLE handle = new WinNT.HANDLE();
                handle.setPointer(Pointer.createConstant(handl));
                return kernel.GetProcessId(handle);

            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                LOG.error("Cannot get Windows pid: " + e.getMessage());
            }
        }

        return null;
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

    public static void writePidToFile(Integer pid, String pidFilename) throws IOException {
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
        if (file.exists()) {
            if (file.canExecute()) {
                return true;
            }
            return file.setExecutable(true);
        }
        return false;
    }

}
