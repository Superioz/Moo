package de.superioz.moo.client.common;

import de.superioz.moo.api.database.DatabaseModifyType;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.DbFilter;
import de.superioz.moo.api.database.DbQuery;
import de.superioz.moo.client.Moo;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.Response;
import de.superioz.moo.protocol.packets.PacketDatabaseInfo;
import de.superioz.moo.protocol.packets.PacketDatabaseInfoNative;
import de.superioz.moo.protocol.packets.PacketDatabaseModify;
import de.superioz.moo.protocol.packets.PacketDatabaseModifyNative;

import java.util.function.Consumer;

/**
 * The database object of the connection to the cloud
 * That's because it's better to only have one connection to the database
 * instead of using multiple connections
 * <p>
 * This class represents the packets-communication in terms of
 * using the database
 */
public class MooDatabase {

    public MooDatabase(Moo moo) {
        // Waiting to do something with moo ..
    }

    /**
     * Queries something from the database
     *
     * @param type     The type of database
     * @param filter   The filter to search for occurrences
     * @param callback The callback of the response
     */
    public void query(DatabaseType type, DbFilter filter, Consumer<Response> callback) {
        PacketMessenger.transferToResponse(new PacketDatabaseInfo(type, filter), callback);
    }

    public Response query(DatabaseType type, DbFilter filter) {
        return PacketMessenger.transferToResponse(new PacketDatabaseInfo(type, filter));
    }

    public void query(String database, DbFilter filter, Consumer<Response> callback) {
        PacketMessenger.transferToResponse(new PacketDatabaseInfoNative(database, filter, -1), callback);
    }

    public Response query(String database, DbFilter filter) {
        return PacketMessenger.transferToResponse(new PacketDatabaseInfoNative(database, filter, -1));
    }

    /**
     * Modifies something from the database
     *
     * @param type       The type of database
     * @param filter     The filter to search for occurrences
     * @param modifyType The modification type
     * @param query      The query
     * @param callback   The callback
     */
    public void modify(DatabaseType type, DbFilter filter, DatabaseModifyType modifyType, DbQuery query,
                       Consumer<Response> callback) {
        PacketMessenger.transferToResponse(new PacketDatabaseModify(type, filter, modifyType, query), callback);
    }

    public Response modify(DatabaseType type, DbFilter filter, DatabaseModifyType modifyType, DbQuery query) {
        return PacketMessenger.transferToResponse(new PacketDatabaseModify(type, filter, modifyType, query));
    }

    public void modify(String database, DbFilter filter, DatabaseModifyType modifyType, DbQuery query,
                       Consumer<Response> callback) {
        PacketMessenger.transferToResponse(new PacketDatabaseModifyNative(database, filter, modifyType, query, -1), callback);
    }

    public Response modify(String database, DbFilter filter, DatabaseModifyType modifyType, DbQuery query) {
        return PacketMessenger.transferToResponse(new PacketDatabaseModifyNative(database, filter, modifyType, query, -1));
    }

    /**
     * Updates something from the database
     *
     * @param type     The type of the database
     * @param filter   The filter
     * @param callback The callback
     */
    public void update(DatabaseType type, DbFilter filter, DbQuery query, Consumer<Response> callback) {
        this.modify(type, filter, DatabaseModifyType.MODIFY, query, callback);
    }

    public Response update(DatabaseType type, DbFilter filter, DbQuery query) {
        return this.modify(type, filter, DatabaseModifyType.MODIFY, query);
    }

    public void update(String database, DbFilter filter, DbQuery query, Consumer<Response> callback) {
        this.modify(database, filter, DatabaseModifyType.MODIFY, query, callback);
    }

    public Response update(String database, DbFilter filter, DbQuery query) {
        return this.modify(database, filter, DatabaseModifyType.MODIFY, query);
    }

    /**
     * Deletes something from the database
     *
     * @param type     The type of the database
     * @param filter   The filter
     * @param callback The callback
     */
    public void delete(DatabaseType type, DbFilter filter, Consumer<Response> callback) {
        this.modify(type, filter, DatabaseModifyType.DELETE, null, callback);
    }

    public Response delete(DatabaseType type, DbFilter filter) {
        return this.modify(type, filter, DatabaseModifyType.DELETE, null);
    }

    public void delete(String database, DbFilter filter, Consumer<Response> callback) {
        this.modify(database, filter, DatabaseModifyType.DELETE, null, callback);
    }

    public Response delete(String database, DbFilter filter) {
        return this.modify(database, filter, DatabaseModifyType.DELETE, null);
    }

    /**
     * Deletes something from the database
     *
     * @param type     The type of the database
     * @param object   The object
     * @param callback The callback
     */
    public void create(DatabaseType type, Object object, Consumer<Response> callback) {
        this.modify(type, DbFilter.fromObjectsPrimKey(object.getClass(), object),
                DatabaseModifyType.CREATE, DbQuery.fromObject(object), callback);
    }

    public Response create(DatabaseType type, Object object) {
        return this.modify(type, DbFilter.fromObjectsPrimKey(object.getClass(), object),
                DatabaseModifyType.CREATE, DbQuery.fromObject(object));
    }

    public void create(String database, Object object, Consumer<Response> callback) {
        this.modify(database, DbFilter.fromObjectsPrimKey(object.getClass(), object),
                DatabaseModifyType.CREATE, DbQuery.fromObject(object), callback);
    }

    public Response create(String database, Object object) {
        return this.modify(database, DbFilter.fromObjectsPrimKey(object.getClass(), object),
                DatabaseModifyType.CREATE, DbQuery.fromObject(object));
    }

}
