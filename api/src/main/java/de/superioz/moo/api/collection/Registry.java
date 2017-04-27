package de.superioz.moo.api.collection;

import java.util.HashMap;
import java.util.Map;

/**
 * Similar to a {@link HashMap} but with other method names
 *
 * @param <K> The key
 * @param <V> The value
 */
public class Registry<K, V> {

    protected Map<K, V> keyObjectMap = new HashMap<>();

    /**
     * Gets the value by given {@code key}
     *
     * @param key The key of the registered values
     * @return The object
     */
    public V get(K key) {
        return keyObjectMap.get(key);
    }

    /**
     * Similar to {@link HashMap#put(Object, Object)} but another name
     *
     * @param key    The key
     * @param object The object to be stored behind the key
     * @return The result
     */
    public boolean register(K key, V object) {
        return keyObjectMap.put(key, object) != null;
    }

    public boolean register(V... objects) {
        boolean r = false;
        for(V object : objects) {
            r = register((K) object.toString(), object);
        }
        return r;
    }

    /**
     * Similar to {@link HashMap#remove(Object, Object)} but another name
     *
     * @param key   The key
     * @param value The value that was stored behind thee key (to-delete)
     * @return The result
     */
    public boolean unregister(K key, V value) {
        return keyObjectMap.remove(key, value);
    }

    public boolean unregister(V... objects) {
        boolean r = false;
        for(V object : objects) {
            r = unregister((K) object.toString(), object);
        }
        return r;
    }

}
