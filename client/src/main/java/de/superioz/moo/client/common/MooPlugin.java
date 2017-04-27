package de.superioz.moo.client.common;

import java.util.function.Function;

/**
 * Represents a plugin which can be loaded with Moo ({@link MooPluginManager})
 */
public interface MooPlugin {

    /**
     * Loads the config file and the properties
     */
    void loadConfig();

    /**
     * Method for registering different stuff (commands, listeners, ...)
     *
     * @param startup The registry
     */
    void loadPluginStartup(MooPluginStartup startup);

    /**
     * Returns a function for a listener which is specifically for spigot or bungee
     *
     * @return The function
     */
    Function<Object, Boolean> registerLeftOvers();

}
