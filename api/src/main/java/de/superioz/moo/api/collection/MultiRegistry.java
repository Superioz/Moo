package de.superioz.moo.api.collection;

import java.util.HashSet;
import java.util.Set;

/**
 * Similar to {@link MultiMap} but with other method names and functions
 *
 * @param <K>
 */
public class MultiRegistry<K, V> {

    protected MultiMap<K, V> keyObjectMap = new MultiMap<>();

    /**
     * Gets a list of value by given {@code key}
     *
     * @param key The key of the registered values
     * @return The list of objects
     */
    public Set<V> get(K key) {
        if(!keyObjectMap.containsKey(key)){
            return new HashSet<V>();
        }
        return keyObjectMap.get(key);
    }

    /**
     * Similar to {@link MultiMap#put(Object, Object)} but another name
     *
     * @param key     The key
     * @param objects The objects to be stored behind the key
     * @return The result
     */
    public boolean register(K key, V... objects) {
        return keyObjectMap.add(key, objects) != null;
    }

    /**
     * Similar to {@link MultiMap#delete(Object, Object)} but another name
     *
     * @param key   The key
     * @param value The value that was stored behind thee key (to-delete)
     * @return The result
     */
    public boolean unregister(K key, V value) {
        return keyObjectMap.delete(key, value) != null;
    }

}
