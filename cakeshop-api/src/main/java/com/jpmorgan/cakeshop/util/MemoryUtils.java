package com.jpmorgan.cakeshop.util;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class MemoryUtils {

    public static Long getMemoryData(Boolean isFreeMemory) {

        OperatingSystemMXBean osMBean
                = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        if (isFreeMemory) {
            return osMBean.getFreePhysicalMemorySize() / 1024;
        } else {
            return osMBean.getTotalPhysicalMemorySize() / 1024;
        }

    }

}
