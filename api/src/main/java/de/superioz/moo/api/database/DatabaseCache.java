package de.superioz.moo.api.database;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mongodb.client.FindIterable;
import de.superioz.moo.api.database.filter.DbFilter;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is a class for caching database content.<br>
 * It makes use of the {@link LoadingCache} by google and it is connected to a {@link DatabaseCollection} to define the key and value
 * stored inside this cache
 *
 * @param <K> The key of the value
 * @param <V> The value
 */
public class DatabaseCache<K, V> {

    /**
     * The wrapped {@link LoadingCache} from Google
     */
    @Getter
    private final LoadingCache<K, V> wrapped;

    /**
     * The Mongo collection this cache is added to
     */
    @Getter
    private final DatabaseCollection<K, V> databaseCollection;

    public DatabaseCache(DatabaseCache.Builder builder) {
        this(builder.databaseCollection, builder.builder);
    }

    protected DatabaseCache(DatabaseCollection<K, V> collection, CacheBuilder<K, V> builder) {
        this.databaseCollection = collection;
        this.wrapped = builder.build(getLoader());
        this.load();
    }

    /**
     * Clears the cache
     */
    public void clear() {
        wrapped.invalidateAll();
    }

    /**
     * Gets the size of the wrapped cache
     *
     * @return The size as long
     */
    public long size() {
        return wrapped.size();
    }

    /**
     * Gets the cache as concurrentMap (thread safety)
     *
     * @return The map
     */
    public ConcurrentMap<K, V> asMap() {
        return wrapped.asMap();
    }

    /**
     * Gets the cache as list
     *
     * @return The list
     */
    public List<V> asList() {
        return new ArrayList<>(asMap().values());
    }

    /**
     * Checks if cache contains key
     *
     * @param key The key
     * @return The successful
     */
    public boolean has(K key) {
        return this.asMap().containsKey(key);
    }

    /**
     * Inserts a key with value
     *
     * @param key   The key
     * @param value The value
     * @return The successful
     */
    public boolean insert(K key, V value) {
        if(key == null) return false;
        wrapped.put(key, value);
        return true;
    }

    /**
     * Removes entry with given key from cache
     *
     * @param key The key
     * @return The successful
     */
    public boolean remove(K key) {
        wrapped.invalidate(key);
        return true;
    }

    /**
     * Get value from key out of the cache
     *
     * @param key The key
     * @return The value
     */
    public V get(K key) {
        try {
            return wrapped.get(key);
        }
        catch(Exception e) {
            return null;
        }
    }

    /**
     * Gets values from cache with given query ({@link DbFilter} will be used as predicate)
     *
     * @param query    The query
     * @param streamed Should the elements in this cache be streamed or looped? true = streamed
     * @return The list of values
     */
    public List<V> get(DbFilter query, boolean streamed) {
        return streamed ? queryStreamed(query.toPredicate()) : queryLooped(query.toPredicate());
    }

    /**
     * Queries the cache with a filter
     *
     * @param filter    The filter
     * @param onElement If the query founds an element
     */
    public void query(Predicate<V> filter, BiConsumer<K, V> onElement) {
        this.asMap().forEach((k, v) -> {
            if(!filter.test(v)) return;

            onElement.accept(k, v);
        });
    }

    /**
     * Queries the map with given filter (warning: loop could be faster with few values)
     * Shortform for query stream
     *
     * @param filter The predicate object for filtering
     * @return List of found values
     */
    public List<V> queryStreamed(Predicate<V> filter) {
        return this.asList().stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Checks if the cache contains elements validated with the predicate<br>
     * Streamed means, that the cache elements will be streamed (1.8+)
     *
     * @param filter The element filter
     * @return The result
     */
    public boolean hasStreamed(Predicate<V> filter) {
        List<V> l = queryStreamed(filter);
        return l != null && !l.isEmpty();
    }

    /**
     * Loops through the map with given filter (warning: streams could be faster with many values)
     * Shortform for query loop
     *
     * @param filter The predicate object for filtering
     * @return List of found values
     */
    public List<V> queryLooped(Predicate<V> filter) {
        List<V> l = new ArrayList<>();
        for(V v : this.asList()) {
            if(filter == null || filter.test(v)) {
                l.add(v);
            }
        }
        return l;
    }

    /**
     * Checks if the cache contains elements validated with the predicate<br>
     * Looped means, that the cache elements will be looped (1.7)
     *
     * @param filter The element filter
     * @return The result
     */
    public boolean hasLooped(Predicate<V> filter) {
        List<V> l = queryLooped(filter);
        return l != null && !l.isEmpty();
    }

    /**
     * Get the loader for (re)loading objects into the cache
     *
     * @return The cacheLoader object
     */
    public CacheLoader<K, V> getLoader() {
        return new CacheLoader<K, V>() {
            @Override
            public V load(K k) throws Exception {
                DbFilter filter = DbFilter.fromPrimKey(DatabaseCache.this.getDatabaseCollection().getWrappedClass(), k);
                FindIterable<Document> result = DatabaseCache.this.getDatabaseCollection().fetch(filter, 1);

                return DatabaseCache.this.getDatabaseCollection().convert(result.first());
            }
        };
    }

    /**
     * Calls if the cache is loaded (idk if you want to do smth, you can do it here :))
     */
    protected void load() {
        //
    }

    /**
     * The builder to build the database cache
     */
    public static class Builder<T extends DatabaseCache> {

        private CacheBuilder builder;
        private DatabaseCollection databaseCollection;

        public Builder() {
            this.builder = CacheBuilder.newBuilder();
        }

        /**
         * Sets the expiration after access of the cache
         *
         * @param duration The duration as value
         * @param unit     The time unit
         * @return This
         */
        public Builder<T> expireAfterAccess(long duration, TimeUnit unit) {
            this.builder.expireAfterAccess(duration, unit);
            return this;
        }

        /**
         * Sets the expiration after writing into the cache
         *
         * @param duration The duration as value
         * @param unit     The time unit
         * @return This
         */
        public Builder<T> expireAfterWrite(long duration, TimeUnit unit) {
            this.builder.expireAfterWrite(duration, unit);
            return this;
        }

        /**
         * Sets the maximum size of the cache
         *
         * @param size The size
         * @return This
         */
        public Builder<T> maximumSize(long size) {
            this.builder.maximumSize(size);
            return this;
        }

        /**
         * Sets the database this cache inherits
         *
         * @param module The database
         * @return This
         */
        public Builder<T> database(DatabaseCollection module) {
            this.databaseCollection = module;
            return this;
        }

    }

}
