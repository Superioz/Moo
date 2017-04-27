package de.superioz.moo.client.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is the registry for {@link MooPlugin}'s
 */
public class MooPluginManager {

    //private static final String CLOUD_ACTIVATION_KEY = "cloud";
    private ConcurrentMap<String, MooPlugin> pluginByName = new ConcurrentHashMap<>();

    /**
     * Get all registered plugins
     *
     * @return The list of plugins
     */
    public List<MooPlugin> getPlugins() {
        return new ArrayList<>(pluginByName.values());
    }

    /**
     * Checks if plugin map contains name
     *
     * @param name The name
     * @return The result
     */
    public boolean contains(String name) {
        return pluginByName.containsKey(name);
    }

    /**
     * Gets the plugin with given name
     *
     * @param name The name
     * @return The plugin
     */
    public MooPlugin get(String name) {
        return pluginByName.get(name);
    }

    /**
     * Registers a plugin
     *
     * @param name   The name
     * @param plugin The plugin
     * @return The plugin
     */
    public MooPlugin register(String name, MooPlugin plugin) {
        return pluginByName.put(name, plugin);
    }

    /**
     * Unregisters a plugin
     *
     * @param key The key
     * @return The plugin
     */
    public MooPlugin unregister(String key) {
        return pluginByName.remove(key);
    }

}
