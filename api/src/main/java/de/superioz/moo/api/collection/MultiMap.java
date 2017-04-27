package de.superioz.moo.api.collection;

import java.util.*;

/**
 * This map is similar to a default {@link HashMap} but with the ability to add multiple values to one key<br>
 * This works with having the value be a list
 *
 * @param <K> The key type
 * @param <V> The value type
 */
public class MultiMap<K, V> extends LinkedHashMap<K, Set<V>> {

    /**
     * Adds a value to a list object inside this map.
     *
     * @param key The key of the list
     * @param val The value to be added to the list
     * @return This
     */
    public MultiMap<K, V> add(K key, V... val) {
        if(key == null) return null;

        Set<V> list = get(key);
        if(list == null) list = new HashSet<>();
        if(val.length > 0) list.addAll(Arrays.asList(val));

        put(key, list);
        return this;
    }

    /**
     * Opposite of {@link #add(Object, Object...)} and similar to {@link #remove(Object, Object)} but with
     * removing the value from the list behind this key
     *
     * @param key The key of the list
     * @param val The value inside the list to be removed
     * @return This
     */
    public MultiMap<K, V> delete(K key, V val) {
        if(key == null || val == null) return null;

        Set list = get(key);
        if(list != null) {
            list.remove(val);
            put(key, list);
        }
        return this;
    }

}
