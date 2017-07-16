package de.superioz.moo.api.utils;

import com.sun.management.OperatingSystemMXBean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.management.ManagementFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SystemUtil {

    /**
     * Checks if the operating system is Windows
     *
     * @return The result
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Gets the current ram usage of this system
     *
     * @return The current usage in per cent
     */
    public static int getCurrentRamUsage() {
        OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        long total = bean.getTotalPhysicalMemorySize() / 1024;
        long used = total - (bean.getFreePhysicalMemorySize() / 1024);

        return (int) (100D / (double)total * (double)used);
    }

    /**
     * Gets the current user
     *
     * @return The user
     */
    public static String getCurrentUser() {
        return System.getProperty("user.name");
    }

    /**
     * Gets the current vm name
     *
     * @return The name
     */
    public static String getVMName() {
        return System.getProperty("java.vm.name");
    }

    /**
     * Gets the current vm version
     *
     * @return The version
     */
    public static String getVMVersion() {
        return System.getProperty("java.vm.version");
    }

    /**
     * Gets the current java version
     *
     * @return The version
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version");
    }

}
