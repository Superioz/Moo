package de.superioz.moo.protocol.common;

import de.superioz.moo.api.database.*;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packets.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.protocol.events.QueryEvent;
import de.superioz.moo.protocol.exception.MooInputException;
import de.superioz.moo.protocol.exception.MooOutputException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Class for easier sending packets to either the currently connected network instance
 * or to another one
 * <br>
 * If you want to specifically get some groups ({@link Group}) data, then
 * use another class, this class is only for abstract data fetching/modifying etc.
 * <br>
 * Similar to a {@link PacketMessenger} for only {@link PacketDatabaseModify}, {@link PacketDatabaseInfo}, ...
 *
 * @see PacketMessenger
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Queries {

    private DatabaseType databaseType;
    private String databaseName;

    private DbFilter filter;
    private DbQuery query;

    private int limit = -1;
    private DatabaseModifyType modifyType;

    private int countMode = -1;

    /**
     * Queries given packet with firing the {@link QueryEvent}
     *
     * @param packet The packet
     */
    public static QueryEvent queryPacket(AbstractPacket packet) {
        QueryEvent event = new QueryEvent(packet);
        EventExecutor.getInstance().execute(event);
        return event;
    }

    /**
     * Returns the database name
     *
     * @return The string
     */
    public String getDatabase() {
        if(databaseType != null) return databaseType.name().toLowerCase();
        else return databaseName;
    }

    /*
    ===========================================

    ===========================================
     */

    /**
     * Receives data from database with given values
     *
     * @param type   The type of the database
     * @param filter The filter to fetch the data
     * @param eClass The element class to cast the data with
     * @param <E>    The element type
     * @return The element
     * @throws MooInputException If the response isn't OK
     */
    public static <E> E get(DatabaseType type, DbFilter filter, Class<E> eClass) throws MooInputException {
        Response response = Queries.newInstance(type).filter(filter).execute();
        if(response == null) {
            return null;
        }
        return response.toComplex(eClass);
    }

    public static <E> E get(DatabaseType type, Class<?> objectClass, Object primKey, Class<E> eClass) throws MooInputException {
        return get(type, DbFilter.fromPrimKey(objectClass, primKey), eClass);
    }

    public static <E> E get(DatabaseType type, Object primKey, Class<E> eClass) throws MooInputException {
        return get(type, type.getWrappedClass(), primKey, eClass);
    }

    public static <E> E get(DatabaseType type, Object primKey) throws MooInputException {
        return get(type, primKey, (Class<E>) type.getWrappedClass());
    }


    /**
     * Similar to {@link #get(DatabaseType, Class, Object, Class)} but with a database name instead of the type
     *
     * @param databaseName The databaseName
     * @param filter       The filter to fetch the data
     * @param eClass       The element class to cast the data with
     * @param <E>          The element type
     * @return The element
     * @throws MooInputException If the response isn't OK
     */
    public static <E> E get(String databaseName, DbFilter filter, Class<E> eClass) throws MooInputException {
        Response response = Queries.newInstance(databaseName).filter(filter).execute();
        if(response == null) {
            return null;
        }
        return response.toComplex(eClass);
    }

    public static <E> E get(String databaseName, Class<?> objectClass, Object primKey, Class<E> eClass) throws MooInputException {
        return get(databaseName, DbFilter.fromPrimKey(objectClass, primKey), eClass);
    }

    /**
     * Receives datalist from database with given values
     *
     * @param type   The type of the database
     * @param filter The filter to fetch the data
     * @param eClass The element class to cast the data with
     * @param <E>    The element type
     * @return The element
     * @throws MooInputException If the response isn't OK
     */
    public static <E> List<E> list(DatabaseType type, DbFilter filter, Class<E> eClass) throws MooInputException {
        Response response = Queries.newInstance(type).filter(filter).execute();
        if(response == null) {
            return null;
        }
        return response.toComplexes(eClass);
    }

    public static <E> List<E> list(DatabaseType type, Class<?> objectClass, Object primKey, Class<E> eClass) throws MooInputException {
        return list(type, DbFilter.fromPrimKey(objectClass, primKey), eClass);
    }

    public static <E> List<E> list(DatabaseType type, Object primKey, Class<E> eClass) throws MooInputException {
        return list(type, type.getWrappedClass(), primKey, eClass);
    }

    public static <E> List<E> list(DatabaseType type, Object primKey) throws MooInputException {
        return list(type, primKey, (Class<E>) type.getWrappedClass());
    }

    /**
     * Lists all elements from database type
     *
     * @param type   The type of the database
     * @param eClass The element class to cast the data with
     * @param <E>    The element type
     * @return The elements
     * @throws MooInputException If the response isn't OK
     */
    public static <E> List<E> list(DatabaseType type, Class<E> eClass) throws MooInputException {
        Response response = Queries.newInstance(type).count(true).execute();
        if(response == null) {
            return null;
        }
        return response.toComplexes(eClass);
    }

    /**
     * Similar to {@link #list(DatabaseType, Class)} but with a database name instead of the type
     *
     * @param databaseName The database name
     * @param eClass       The element class to cast the data with
     * @param <E>          The element type
     * @return The elements
     * @throws MooInputException If the response isn't OK
     */
    public static <E> List<E> list(String databaseName, Class<E> eClass) throws MooInputException {
        Response response = Queries.newInstance(databaseName).count(true).execute();
        if(response == null) {
            return null;
        }
        return response.toComplexes(eClass);
    }

    /**
     * Creates data into database with given type
     *
     * @param type   The database type
     * @param object The object to be converted into a {@link DbQuery}
     * @return The response of this task
     */
    public static Response create(DatabaseType type, Object object) {
        return Queries.newInstance(type).creation(object).execute();
    }

    /**
     * Similar to {@link #create(DatabaseType, Object)} but with a database name instead of the type
     *
     * @param databaseName The databaseName
     * @param object       The object to be converted into a {@link DbQuery}
     * @return The response of this task
     */
    public static Response create(String databaseName, Object object) {
        return Queries.newInstance(databaseName).creation(object).execute();
    }

    /**
     * Deletes data from the database with the filter
     *
     * @param type   The database type
     * @param filter The filter to fetch the data with to delete
     * @return The response of this task
     */
    public static Response delete(DatabaseType type, DbFilter filter) {
        return Queries.newInstance(type).filter(filter).deletion().execute();
    }

    public static Response delete(DatabaseType type, Class<?> objectClass, Object primKey) {
        return delete(type, DbFilter.fromPrimKey(objectClass, primKey));
    }

    public static Response delete(DatabaseType type, Object primKey) {
        return delete(type, type.getWrappedClass(), primKey);
    }

    /**
     * Similar to {@link #delete(DatabaseType, DbFilter)} but with a database name instead of the type
     *
     * @param databaseName The database name
     * @param filter       The filter to fetch the data with to delete
     * @return The response of this task
     */
    public static Response delete(String databaseName, DbFilter filter) {
        return Queries.newInstance(databaseName).filter(filter).deletion().execute();
    }

    public static Response delete(String databaseName, Class<?> objectClass, Object primKey) {
        return delete(databaseName, DbFilter.fromPrimKey(objectClass, primKey));
    }

    /**
     * Modifies data from the database with given values
     *
     * @param type   The database type
     * @param filter The filter to fetch the data with to modify
     * @param query  The query to modify the data
     * @return The response of this task
     */
    public static Response modify(DatabaseType type, DbFilter filter, DbQuery query) {
        return Queries.newInstance(type).filter(filter).update(query).execute();
    }

    public static Response modify(DatabaseType type, Class<?> objectClass, Object primKey, DbQuery query) {
        return modify(type, DbFilter.fromPrimKey(objectClass, primKey), query);
    }

    public static Response modify(DatabaseType type, Object primKey, DbQuery query) {
        return modify(type, type.getWrappedClass(), primKey, query);
    }

    public static Response modify(DatabaseType type, DbFilter filter, DbQueryUnbaked query) {
        return modify(type, filter, query.bake(type.getWrappedClass()));
    }

    public static Response modify(DatabaseType type, Class<?> objectClass, Object primKey, DbQueryUnbaked query) {
        return modify(type, DbFilter.fromPrimKey(objectClass, primKey), query);
    }

    public static Response modify(DatabaseType type, Object primKey, DbQueryUnbaked query) {
        return modify(type, type.getWrappedClass(), primKey, query);
    }

    /*
    =======================================
    CREATE RAW QUERIES
    =======================================
     */

    public static Queries newInstance() {
        return new Queries();
    }

    public static Queries newInstance(DatabaseType databaseType) {
        return newInstance().scope(databaseType);
    }

    public static Queries newInstance(Class<?> clazz) {
        return newInstance().scope(clazz);
    }

    public static Queries newInstance(String database) {
        return newInstance().scope(database);
    }


    /**
     * Sets the limit of the query, that means the maximum amount of entries fetched or edited
     *
     * @param i The int as limit (<0 = -1)
     * @return This
     */
    public Queries limit(int i) {
        if(i < 0) i = -1;
        this.limit = i;
        return this;
    }

    /**
     * Determines that this query is for counting the database's entries
     *
     * @param asList As list would return a list of objects otherwise only a number
     * @return This
     */
    public Queries count(boolean asList) {
        this.countMode = asList ? 1 : 0;
        return this;
    }

    /**
     * Defines the database scope<br>
     * Other methods to do that: {@link #scope(Class)} and {@link #scope(String)}
     *
     * @param databaseType The databaseType
     * @return This
     */
    public Queries scope(DatabaseType databaseType) {
        if(databaseName == null) {
            this.databaseType = databaseType;
        }
        return this;
    }

    /**
     * Similar to {@link #scope(DatabaseType)} but with getting the DatabaseType by Class.<br>
     * e.g.: {@link PlayerData}.class would result in {@link DatabaseType#PLAYER}
     *
     * @param clazz The class of the database object
     * @return This
     * @see #scope(DatabaseType)
     */
    public Queries scope(Class<?> clazz) {
        for(DatabaseType t : DatabaseType.values()) {
            if(t.getWrappedClass().equals(clazz)) {
                return scope(t);
            }
        }
        return this;
    }

    /**
     * Similar to {@link #scope(DatabaseType)} but with a raw database name (for custom database actions)
     *
     * @param database The database name (e.g. "client-accounts")
     * @return This
     * @see #scope(DatabaseType)
     */
    public Queries scope(String database) {
        if(databaseType == null) {
            this.databaseName = database;
        }
        return this;
    }

    /**
     * Sets the filter of the query to find entries (and either modify or fetch them)
     *
     * @param filter The filter
     * @return This
     */
    public Queries filter(DbFilter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Similar to {@link #filter(DbFilter)} but with creating a {@link DbFilter} from given values<br>
     * Example usage: objClass = "PlayerData.class"; primKey = "550e8400-e29b-11d4-a716-446655440000"
     *
     * @param objClass The objects class (example above)
     * @param primKey  The primary key (e.g. an uuid)
     * @return This
     */
    public Queries filter(Class<?> objClass, Object primKey) {
        return filter(DbFilter.fromPrimKey(objClass, primKey));
    }

    /**
     * Similar to {@link #filter(DbFilter)} but with creating a {@link DbFilter} from given values<br>
     * The primKey is the primary field from before setted class
     *
     * @param primKey The primary key (e.g. an uuid)
     * @return This
     */
    public Queries filter(Object primKey) {
        Class<?> c = databaseType != null ? databaseType.getWrappedClass() : null;
        if(c == null) return this;
        return filter(DbFilter.fromPrimKey(c, primKey));
    }

    /**
     * Determines that the query is creating an object
     *
     * @param object The object
     * @return This
     */
    public Queries creation(Object object) {
        if(filter != null) {
            return this;
        }
        if(databaseType == null) scope(object.getClass());

        this.modifyType = DatabaseModifyType.CREATE;
        this.query = DbQuery.fromObject(object);
        return filter(DbFilter.fromObjectsPrimKey(object.getClass(), object));
    }

    /**
     * Determines that the query is deleting an object
     *
     * @return This
     */
    public Queries deletion() {
        this.modifyType = DatabaseModifyType.DELETE;
        return this;
    }

    /**
     * Determines that this query is updating something and what<br>
     * For simplier update definition use {@link #update(String, DbQueryNode.Type, Object, Validation...)}
     * or {@link #update(DbModifier, DbQueryNode.Type, Object)}
     *
     * @param query The query
     * @return This
     * @see #deletion()
     * @see #creation(Object)
     */
    public Queries update(DbQuery query) {
        this.modifyType = DatabaseModifyType.MODIFY;
        this.query = query;
        return this;
    }

    /**
     * Similar to {@link #update(DbQuery)} but with query creation from given values
     *
     * @param modifier  The modifier
     * @param operation The operation
     * @param obj       The object to be operated
     * @return This
     */
    public Queries update(DbModifier modifier, DbQueryNode.Type operation, Object obj) {
        this.modifyType = DatabaseModifyType.MODIFY;
        if(query != null) {
            query.add(modifier, operation, obj);
        }
        return this;
    }

    /**
     * Similar to {@link #update(DbQuery)} but with query creation from given values
     *
     * @param fieldName   The name of the field to be updated
     * @param operation   The operation
     * @param obj         The object to be operated
     * @param validations The object validations
     * @return This
     */
    public Queries update(String fieldName, DbQueryNode.Type operation, Object obj, Validation... validations) {
        this.modifyType = DatabaseModifyType.MODIFY;
        if(query != null) {
            List<Integer> validationIds = new ArrayList<>();
            for(Validation validation : validations) {
                validationIds.add(validation.ordinal());
            }

            query.add(new DbQueryNode(fieldName, operation, validationIds, obj));
        }
        return this;
    }

    /**
     * Similar to {@link #update(DbModifier, DbQueryNode.Type, Object)} but without defining a node type
     *
     * @param modifier The modifier
     * @param obj      The object to be operated
     * @return This
     */
    public Queries equate(DbModifier modifier, Object obj) {
        return update(modifier, DbQueryNode.Type.EQUATE, obj);
    }

    public Queries equate(String fieldName, Object obj, Validation... validations) {
        return update(fieldName, DbQueryNode.Type.EQUATE, obj, validations);
    }

    /**
     * Similar to {@link #update(DbModifier, DbQueryNode.Type, Object)} but without defining a node type
     *
     * @param modifier The modifier
     * @param obj      The object to be operated
     * @return This
     */
    public Queries add(DbModifier modifier, Object obj) {
        return update(modifier, DbQueryNode.Type.APPEND, obj);
    }

    public Queries add(String fieldName, Object obj, Validation... validations) {
        return update(fieldName, DbQueryNode.Type.APPEND, obj, validations);
    }

    /**
     * Similar to {@link #update(DbModifier, DbQueryNode.Type, Object)} but without defining a node type
     *
     * @param modifier The modifier
     * @param obj      The object to be operated
     * @return This
     */
    public Queries sub(DbModifier modifier, Object obj) {
        return update(modifier, DbQueryNode.Type.SUBTRACT, obj);
    }

    public Queries sub(String fieldName, Object obj, Validation... validations) {
        return update(fieldName, DbQueryNode.Type.SUBTRACT, obj, validations);
    }

    /**
     * Executes the {@link QueryEvent} to send or simulate a modify/info packet
     *
     * @return The response
     */
    public Response execute() throws MooOutputException {
        QueryEvent event = Queries.queryPacket(toPacket());

        if(event.isCancelled()) {
            if(event.getCancelReason() != null){
                try {
                    throw event.getCancelReason();
                }
                catch(Throwable throwable) {
                    if(throwable instanceof MooOutputException) {
                        throw (MooOutputException) throwable;
                    }
                    // nothing otherwise
                }
            }
            return null;
        }
        return event.getSupplier().get(3, TimeUnit.SECONDS);
    }

    /**
     * Turns every saved value from this builder to a packet to be sent or simulated
     *
     * @return The packet
     */
    public AbstractPacket toPacket() {
        if(countMode != -1) {
            return new PacketDatabaseCount(databaseType, countMode, limit);
        }

        boolean info = modifyType == null;
        boolean raw = databaseType == null;

        if(info) {
            return raw ? new PacketDatabaseInfoNative(databaseName, filter, limit)
                    : new PacketDatabaseInfo(databaseType, filter, false, limit);
        }
        else {
            return raw ? new PacketDatabaseModifyNative(databaseName, filter, modifyType, query, limit)
                    : new PacketDatabaseModify(databaseType, filter, modifyType, query, false, limit);
        }
    }

}
