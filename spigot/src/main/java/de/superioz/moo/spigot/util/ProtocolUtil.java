package de.superioz.moo.spigot.util;

import org.bukkit.Bukkit;

/**
 * Created on 12.11.2016.
 */
public class ProtocolUtil {

    /**
     * Gets nms class with given name
     *
     * @param exactName The name
     * @return The class
     */
    public static Class<?> getNMSClassExact(String exactName) {
        Class<?> clazz;
        try {
            clazz = Class.forName(getNMSPackage() + "." + exactName);
        }
        catch(Exception e) {
            return null;
        }

        return clazz;
    }

    /**
     * Gets obc class with given name
     *
     * @param exactName The name
     * @return The class
     */
    public static Class<?> getOBCClassExact(String exactName) {
        Class<?> clazz;
        try {
            clazz = Class.forName(getOBCPackage() + "." + exactName);
        }
        catch(Exception e) {
            return null;
        }

        return clazz;
    }

    /**
     * Get the package of the nms
     *
     * @return The name
     */
    public static String getNMSPackage() {
        return "net.minecraft.server." + getVersion();
    }

    /**
     * Get the package of the obc
     *
     * @return The name
     */
    public static String getOBCPackage() {
        return "org.bukkit.craftbukkit." + getVersion();
    }

    /**
     * Get version of current bukkit
     *
     * @return The version as string
     */
    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

}
