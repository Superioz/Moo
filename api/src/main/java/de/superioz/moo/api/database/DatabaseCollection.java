package de.superioz.moo.api.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.collection.FixedSizeList;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.filter.DbFilterNode;
import de.superioz.moo.api.database.object.DataArchitecture;
import de.superioz.moo.api.database.object.DataResolver;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.database.query.DbQueryUnbaked;
import de.superioz.moo.api.keyvalue.FinalValue;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;
import org.bson.Document;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a collection from the database with a key and an element type<br>
 * It edits the database collection with using the {@link DatabaseConnection} class.<br>
 * To cache data, {@link DatabaseCache} is the best choice, because it can dynamically store abstract data<br>
 * To make filtering easier, a {@link DataArchitecture} of the wrapped object is made to be able to
 * use indexes instead of strings as keys.
 *
 * @param <K> The key type
 * @param <E> The element type
 */
@Getter
public abstract class DatabaseCollection<K, E> {

    /**
     * The connection to the database
     */
    private DatabaseConnection connection;

    /**
     * The cache to store values temporarily
     */
    private DatabaseCache<K, E> cache;

    /**
     * The architecture of the class' object
     */
    private DataArchitecture architecture;

    /**
     * The class this architecture wraps in
     */
    private Class<?> wrappedClass;

    public DatabaseCollection(DatabaseConnection connection) {
        this.connection = connection;
    }

    /**
     * Get data for packetmodify and packetinfo, where eventually the playername needs to be converted
     * to a uuid. This is the only purpose: Getting information out of the database + converting the playername (if exists)
     *
     * @param playerDataCache The playerData Cache
     * @param filter          The filter
     * @param queried         The queried
     * @param limit           The limit
     * @return The objects
     */
    public List<Object> getFilteredData(DatabaseCollection<UUID, PlayerData> playerDataCache, DbFilter filter, boolean queried, int limit) {
        List<Object> data = limit == -1 ? new ArrayList<>() : new FixedSizeList<>(limit);
        filter = filter.replaceBinaryUniqueIds();

        DbFilterNode n = filter.getKey(0, getWrappedClass());
        Field field = ReflectionUtil.getFieldFromId(0, getWrappedClass());

        if(filter.getSize() == 1 && n != null) {
            Object o = n.getContent();
            if(o == null) return data;

            // HAS UNIQUEID
            if(Validation.UNIQUEID.matches(o.toString())) {
                o = UUID.fromString(o.toString());
            }
            // CONVERT THE NAME INTO A UNIQUEID
            if(field.getType().equals(UUID.class) && !Validation.UNIQUEID.matches(o.toString())) {
                for(PlayerData pd : playerDataCache.getCache().asList()) {
                    if(o.toString().equals(pd.lastName)) {
                        o = pd.uuid;
                    }
                }
            }

            E e = this.get((K) o);
            if(e != null) data.add(e);
        }
        else {
            data.addAll(this.list(filter, queried, limit));
        }

        return data;
    }

    /**
     * Gets the name of the collection
     *
     * @return The collection name
     */
    public abstract String getName();

    /**
     * Gets a collection with this's name
     *
     * @return The mongo collection
     */
    public MongoCollection<Document> getCollection() {
        return getConnection().getCollection(getName());
    }

    /**
     * Sets the cache for this modules
     *
     * @param c The cache
     * @return This
     */
    public DatabaseCollection<K, E> cache(DatabaseCache<K, E> c) {
        this.cache = c;
        return this;
    }

    /**
     * Checks if the cache exists. If not, then this collection is not cacheable
     *
     * @return The result
     */
    public boolean isCacheable() {
        return cache != null;
    }

    /**
     * Sets the architecture
     *
     * @param c The class
     * @return This
     */
    public DatabaseCollection<K, E> architecture(Class<?> c) {
        this.wrappedClass = c;
        this.architecture = DataArchitecture.fromClass(c);
        return this;
    }

    /**
     * Appends given objects to given document. The key of the objects is the field name with current keyNum<br>
     * That means the objects are field objects from this's wrapped class
     *
     * @param doc     The document
     * @param keyNum  The key index
     * @param objects The objects
     * @return The document with appended objects
     */
    public Document append(Document doc, int keyNum, Object... objects) {
        for(Object o : objects) {
            doc.append(getKey(keyNum), o);
            keyNum++;
        }
        return doc;
    }

    /**
     * Gets a key forward given index and classType (database-architecture)
     *
     * @param index The index
     * @param c     The classType
     * @param <T>   The type of key
     * @return The key as string
     */
    private <T> String getKey(int index, Class<T> c) {
        return getArchitecture().resolve(index, c);
    }

    public String getKey(int index) {
        return getArchitecture().resolve(index).getKey();
    }

    /**
     * Converts a document to the type
     *
     * @param doc The document
     */
    public E convert(Document doc) {
        if(doc == null) return null;
        return new DataResolver(getArchitecture()).doc(doc).complete(getWrappedClass());
    }

    /**
     * Converts the type to a document
     *
     * @param e The element
     * @return The document
     */
    public Document convert(E e) {
        if(e == null) return null;
        try {
            return new DataResolver(getArchitecture()).fullResolve(e);
        }
        catch(Exception ex) {
            return null;
        }
    }

    /**
     * Gets an object from the cache
     *
     * @param key The key
     * @return The element
     */
    public E get(K key) {
        if(!isCacheable()) return null;
        return getCache().get(key);
    }

    public E get(DbFilter query) {
        List<E> l = list(query);
        if(l == null || l.isEmpty()) return null;
        return l.get(0);
    }

    /**
     * Get objects from the cache/database
     *
     * @param query   The query to filter
     * @param queried Queried or looped access?
     * @param limit   Limit of objects
     * @return The list of elements
     */
    public List<E> list(DbFilter query, boolean queried, int limit, boolean cached) {
        List<E> l = limit == -1 ? new ArrayList<>() : new FixedSizeList<>(limit);

        try {
            if(isCacheable() && cached) {
                List l1 = getCache().get(query, queried);
                l.addAll(l1);
            }

            if(l.isEmpty()) {
                FindIterable<Document> documents = fetch(query, limit);

                for(Document d : documents) {
                    if(d != null && !d.isEmpty()) {
                        l.add(convert(d));
                    }
                }
            }
        }
        catch(Exception e) {
            System.err.println("Error while fetching data from " + getClass().getSimpleName() + " module:");
            e.printStackTrace();
        }

        if(l.size() == 1 && l.get(0) == null) l = new ArrayList<>();
        return l;
    }

    public List<E> list(DbFilter query, boolean queried, int limit) {
        return list(query, queried, limit, true);
    }

    public List<E> list(DbFilter query) {
        return list(query, false, -1);
    }


    /**
     * CACHE & DATABASE<br>
     * Sets a key value pair into the cache and the database
     *
     * @param key     The key
     * @param element The element
     * @param force   Force the update onto the database, too?
     * @return The result
     */
    public boolean set(K key, E element, Document updates, boolean force) {
        boolean f = true;
        if(isCacheable()) {
            f = getCache().insert(key, element);
        }
        if(force) {
            f = this.update(DbFilter.fromKey(getWrappedClass(), 0, key), updates, true) != 0;
        }
        return f;
    }

    public boolean set(K key, E element, DbQueryUnbaked query, boolean force) {
        return set(key, element, query.bake(getWrappedClass()).toDocument(), force);
    }

    public boolean set(K key, E element, DbQueryUnbaked query) {
        return set(key, element, query.bake(getWrappedClass()).toDocument(), true);
    }

    public boolean set(K key, E element, boolean force) {
        return set(key, element, DbQuery.fromObject(element).toMongoQuery().build(), force);
    }

    public boolean set(K key, E element) {
        return set(key, element, DbQuery.fromObject(element).toMongoQuery().build(), true);
    }

    /**
     * CACHE & DATABASE<br>
     * Resets a value with setting a new primary key and normal updating the value
     *
     * @param oldKey  The old key of the value
     * @param newKey  The new key of the value
     * @param element The element behind the key (/value)
     * @param updates The updates to be applied to the value
     * @param force   Force the update onto the database, too?
     * @return The result
     * @see #set(Object, Object, Document, boolean)
     */
    public boolean set(K oldKey, K newKey, E element, Document updates, boolean force) {
        boolean f = true;
        if(isCacheable()) {
            f = getCache().remove(oldKey) && set(newKey, element, updates, false);
        }
        if(force) {
            f = f && this.update(DbFilter.fromKey(getWrappedClass(), 0, oldKey), updates, true) != 0;
        }

        return f;
    }

    /**
     * Removes something from the cache (& the database) per filter
     *
     * @param filter    The filter to list the data to be removed
     * @param force     Force the update onto the database, too?
     * @param onElement Consumer on every key-value pair removed
     * @return The result
     */
    public boolean unset(DbFilter filter, boolean force, BiConsumer<K, E> onElement) {
        FinalValue<Boolean> r = new FinalValue<>(false);
        if(isCacheable()) {
            getCache().query(filter.toPredicate(), (k, e) -> {
                r.set(getCache().remove(k));

                onElement.accept(k, e);
            });
        }
        if(force) {
            r.set(delete(filter) != 0);
        }
        return r.get();
    }

    /**
     * Creates an entry with given key and the element behind it
     *
     * @param key     The key
     * @param element The element
     * @param force   Force the update onto the database, too?
     * @return The result
     */
    public boolean create(K key, E element, boolean force) {
        if(key == null || key.equals("null")) return false;
        boolean f = true;
        if(isCacheable()) {
            f = getCache().insert(key, element);
        }
        if(force) {
            this.insert(element);
        }
        return f;
    }

    /**
     * Deletes a key from the cache (and the database)
     *
     * @param key   The key
     * @param force Force the update onto the database, too?
     * @return How many fields have been deleted
     */
    public boolean delete(K key, boolean force) {
        boolean f = true;
        if(isCacheable()) {
            f = getCache().remove(key);
        }
        if(force) {
            f = this.delete(DbFilter.fromKey(getWrappedClass(), 0, key)) != 0;
        }
        return f;
    }

    /**
     * Checks if the key is inside the database/the cache
     *
     * @param key    The key
     * @param cached Also include the cache in search?
     * @return The result
     */
    public boolean has(K key, boolean cached) {
        if(isCacheable() && cached) {
            return getCache().has(key);
        }
        return this.has(DbFilter.fromKey(getWrappedClass(), 0, key));
    }

    public boolean has(K key) {
        return has(key, true);
    }

    /**
     * Fetches information from the database with given query (key & values that needs to be true)
     *
     * @param filter The query
     * @param limit  The limit
     */
    public void fetch(DbFilter filter, int limit, Consumer<FindIterable<Document>> callback) {
        getConnection().find(getCollection(), filter == null ? null : filter.toBson(), documents -> {
            if(limit != -1) documents.limit(limit);
            callback.accept(documents);
        });
    }

    public FindIterable<Document> fetch(DbFilter query, int limit) {
        FindIterable<Document> iterable = getConnection().findSync(getCollection(), query == null ? null : query.toBson());
        if(limit != -1) iterable.limit(limit);
        return iterable;
    }

    /**
     * Updates an object from the database with given keys&values
     *
     * @param filter   The query to search for the "to-update" object
     * @param updates  The updates
     * @param callback The callback
     */
    public void update(DbFilter filter, Document updates, Consumer<Long> callback, boolean upsert) {
        if(updates.size() == 0) {
            callback.accept(0L);
            return;
        }
        if(upsert) getConnection().upsert(getCollection(), filter.toBson(), updates, callback);
        else getConnection().update(getCollection(), filter.toBson(), updates, callback);
    }

    public Long update(DbFilter filter, Document updates, boolean upsert) {
        if(updates.size() == 0) {
            return 0L;
        }
        if(upsert) return getConnection().upsertSync(getCollection(), filter.toBson(), updates);
        else return getConnection().updateSync(getCollection(), filter.toBson(), updates);
    }

    /**
     * Inserts documents into the database
     *
     * @param documents The documents
     */
    public void insert(Document... documents) {
        if(documents.length == 0) return;
        getConnection().insertMany(getCollection(), Arrays.asList(documents));
    }

    @SafeVarargs
    public final void insert(E... elements) {
        Document[] docs = new Document[elements.length];
        for(int i = 0; i < elements.length; i++) {
            E element = elements[i];
            if(element == null
                    || ReflectionUtil.getFieldObject(0, element) == null) continue;

            docs[i] = convert(elements[i]);
        }
        insert(docs);
    }

    /**
     * Deletes objects from database with given query
     *
     * @param filter   The filter
     * @param callback The callback
     */
    public void delete(DbFilter filter, Consumer<Long> callback) {
        getConnection().deleteMany(getCollection(), filter.toBson(), callback);
    }

    public Long delete(DbFilter filter) {
        return getConnection().deleteManySync(getCollection(), filter.toBson());
    }

    /**
     * Counts objects from the database with given filteer
     *
     * @param filter   The query
     * @param callback The callback
     */
    public void count(DbFilter filter, Consumer<Long> callback) {
        if(filter == null) {
            getConnection().count(getCollection(), callback);
        }
        else {
            getConnection().count(getCollection(), filter.toBson(), callback);
        }
    }

    public Long count(DbFilter filter) {
        if(filter == null) {
            return getConnection().count(getCollection());
        }
        else {
            return getConnection().count(getCollection(), filter.toBson());
        }
    }

    /**
     * Checks if object with given filter exists
     *
     * @param filter   The filter
     * @param callback The callback
     */
    public void has(DbFilter filter, Consumer<Boolean> callback) {
        this.count(filter, size -> callback.accept(size != 0));
    }

    public boolean has(DbFilter filter) {
        return this.count(filter) != 0;
    }

}
