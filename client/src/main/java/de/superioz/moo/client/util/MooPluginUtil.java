package de.superioz.moo.client.util;

import de.superioz.moo.api.exceptions.InvalidConfigException;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooPlugin;
import de.superioz.moo.client.exception.MooInitializationException;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.client.common.MooDependent;
import de.superioz.moo.client.common.MooPluginStartup;

import java.io.File;
import java.nio.file.Path;

/**
 * A util class for {@link MooPlugin}'s
 */
public class MooPluginUtil {

    private static final String CLOUD_ACTIVATION_CONFIG = "cloud";

    /**
     * Prepares the plugin
     *
     * @param plugin The plugin
     */
    public static void preparePlugin(MooPlugin plugin) {
        MooPluginStartup startup = new MooPluginStartup();
        plugin.loadPluginStartup(startup);
        startup.execute(plugin);
    }

    /**
     * Checks if {@link Moo} is alright
     *
     * @return The result
     */
    public static boolean checkMoo() {
        if(Moo.getInstance() == null) throw new MooInitializationException();
        return Moo.getInstance().isActivated();
    }

    /**
     * Loads the config
     *
     * @param folder The dataFolder
     * @param name   The name of the config file (without .json)
     * @return The config object
     */
    public static JsonConfig loadConfig(Path folder, String name) {
        checkMoo();

        // .
        Moo.getInstance().getLogger().info("Loading configuration ..");
        JsonConfig config = new JsonConfig(name, folder);
        config.load(true, true);

        // .
        if(config.isLoaded()) {
            try {
                boolean cloudActivated = config.get(CLOUD_ACTIVATION_CONFIG);
                Moo.getInstance().setActivated(cloudActivated);
            }
            catch(InvalidConfigException ex) {
                Moo.getInstance().setActivated(true);
            }
        }

        return config;
    }

    public static JsonConfig loadConfig(File folder, String name) {
        return loadConfig(folder.toPath(), name);
    }

    /**
     * Checks if the object is happy with the preconditions
     *
     * @param o The object
     * @return The result
     */
    public static boolean hasMooDependency(Object o) {
        checkMoo();

        boolean mood = isMooDependent(o);
        return !mood || (Moo.getInstance().isConnected() && Moo.getInstance().isActivated() && mood);
    }

    /**
     * Checks if a class is 'mood'. That means the class needs the cloud connection
     *
     * @param o The object
     * @return The result
     */
    public static boolean isMooDependent(Object o) {
        return o.getClass().isAnnotationPresent(MooDependent.class);
    }

}
