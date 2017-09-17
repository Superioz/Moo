package de.superioz.moo.api.collection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A cache which uses a first key to store an {@link net.jodah.expiringmap.ExpiringMap} behind this key.<br>
 * This map is used to store values time-based, that means that this values expires at some point
 *
 * @param <F> The first key (Mostly {@link java.util.UUID})
 * @param <K> The key (e.g. String)
 * @param <V> The value (e.g. Object for accepting every value)
 */
@Getter
public class MultiCache<F, K, V> {

    private static final long DEFAULT_EXPIRATION = 3 * 60;

    private Cache<F, ExpiringMap<K, V>> handle;
    private Map<K, List<Consumer<V>>> removalListenerMap;

    public MultiCache(long expireAfterWrite, long expireAfterAccess) {
        this.handle = CacheBuilder.<F, Cache<K, V>>newBuilder()
                .expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS)
                .expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS)
                .build();
        this.removalListenerMap = new HashMap<>();
    }

    public MultiCache() {
        this(DEFAULT_EXPIRATION, DEFAULT_EXPIRATION);
    }

    /**
     * Puts given value into the cache with nested key of {@code firstKey}.{@code key}<br>
     *
     * @param firstKey         The first key of the cache
     * @param key              The key
     * @param value            The value
     * @param duration         The duration
     * @param unit             The time unit for the duration
     * @param removalListeners The listeners when the value is removed from the cache
     * @return The result
     */
    public boolean put(F firstKey, K key, V value,
                       ExpiringMap.ExpirationPolicy policy, long duration, TimeUnit unit,
                       Consumer<V>... removalListeners) {
        if(firstKey == null || key == null || value == null) return false;
        ExpiringMap<K, V> expiringMap = handle.getIfPresent(firstKey);
        if(expiringMap == null) {
            expiringMap = buildExpiringMap();
        }
        if(duration == -1 || unit == null || policy == null) {
            expiringMap.put(key, value);
        }
        else {
            expiringMap.put(key, value, policy, duration, unit);
        }
        handle.put(firstKey, expiringMap);

        removalListenerMap.put(key, Arrays.asList(removalListeners));
        return true;
    }

    public boolean put(F firstKey, K key, V value, Consumer<V>... removalListeners) {
        return put(firstKey, key, value, null, -1, null, removalListeners);
    }

    /**
     * Gets the value from given first key and second nested key
     *
     * @param firstKey The first key
     * @param key      The second key
     * @return The value
     */
    public V get(F firstKey, K key) {
        if(firstKey == null || key == null) return null;
        ExpiringMap<K, V> cache = handle.getIfPresent(firstKey);
        if(cache == null) return null;
        return cache.get(key);
    }

    /**
     * Checks if the cache contains given keys
     *
     * @param firstKey The first key
     * @param key      The second key
     * @return The result
     */
    public boolean contains(F firstKey, K key) {
        if(firstKey == null || key == null) return false;
        ExpiringMap<K, V> cache = handle.getIfPresent(firstKey);
        return cache != null && cache.containsKey(key);
    }

    /**
     * Removes a value
     *
     * @param firstKey The first key
     * @param key      The second key
     * @return The removed value (null for none)
     */
    public V remove(F firstKey, K key) {
        if(firstKey == null || key == null) return null;
        ExpiringMap<K, V> cache = handle.getIfPresent(firstKey);
        if(cache == null) return null;
        return cache.remove(key);
    }

    /**
     * Removes every value which key is starts with the prefix given
     *
     * @param firstKey The firstkey
     * @param prefix   The prefix
     */
    public void removeSimilar(F firstKey, String prefix) {
        if(firstKey == null || prefix == null) return;
        ExpiringMap<K, V> cache = handle.getIfPresent(firstKey);
        if(cache == null) return;

        for(K key : cache.keySet()) {
            String s = key + "";
            if(s.startsWith(prefix)) cache.remove(key);
        }
    }

    /**
     * Builds a new expiring map
     *
     * @return The map
     */
    private ExpiringMap<K, V> buildExpiringMap() {
        return ExpiringMap.builder()
                .variableExpiration()
                .expirationListener((ExpiringMap.ExpirationListener<K, V>) (k, v) -> {
                    List<Consumer<V>> consumers = removalListenerMap.get(k);
                    if(consumers != null) {
                        for(Consumer<V> c : consumers) {
                            c.accept(v);
                        }
                        removalListenerMap.remove(k);
                    }
                }).build();
    }

}
