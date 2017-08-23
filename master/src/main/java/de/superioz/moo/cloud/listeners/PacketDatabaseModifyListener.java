package de.superioz.moo.cloud.listeners;

import com.mongodb.client.MongoCollection;
import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.collection.MultiMap;
import de.superioz.moo.api.database.*;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.filter.DbFilterNode;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.database.query.MongoQuery;
import de.superioz.moo.api.keyvalue.FinalValue;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketDatabaseModify;
import de.superioz.moo.protocol.packets.PacketDatabaseModifyNative;
import de.superioz.moo.protocol.packets.PacketRequest;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PacketDatabaseModifyListener implements PacketAdapter {

    @PacketHandler
    public void onDatabaseModify(PacketDatabaseModify packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().getDatabaseConnection().isConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }

        // values from packet
        DatabaseType dbType = packet.databaseType;
        DbFilter filter = packet.filter.replaceStandardUniqueIds();
        DbFilter realFilter = packet.filter.replaceBinaryUniqueIds();
        DatabaseModifyType type = packet.type;
        DbQuery updates = packet.updates;

        // what r u doing m8?
        if(dbType == null || type == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        // list collection
        DatabaseCollection module = Cloud.getInstance().getDatabaseCollection(dbType);
        Cloud.getInstance().getLogger().debug("Attempting to modify " + dbType.name() + " modules .. (" + type.name() + ")." +
                " With filter (as " + filter + ") and query (as " + updates + ")");

        // primary key of the filter
        DbFilterNode firstNode = realFilter.getKey(0, module.getWrappedClass());
        Object primaryKey = firstNode == null ? null : firstNode.getContent();

        // list data from filtering
        List<Object> data = module.getFilteredData(DatabaseCollections.PLAYER, filter, packet.queried, packet.limit);
        Cloud.getInstance().getLogger().debug("Found data(" + data.size() + ") to " + type.name() + ": " + data);

        // for informing minecraft
        FinalValue<Boolean> result = new FinalValue<>(true);
        MultiMap<DatabaseModifyType, Object> updatedData = new MultiMap<>();

        // create some data
        Reaction.react(type, DatabaseModifyType.CREATE, () -> {
            // data already exists
            if(!data.isEmpty()) {
                packet.respond(ResponseStatus.CONFLICT);
                return;
            }

            // values are invalid
            if(updates == null
                    || !(filter.getSize() == 1 && firstNode != null)
                    || firstNode.getContent() == null) {
                packet.respond(ResponseStatus.BAD_REQUEST);
                return;
            }
            Object instance = ReflectionUtil.getInstance(module.getWrappedClass());

            // execute creation
            updates.apply(instance);
            module.create(primaryKey, instance, true);

            // add newdata
            updatedData.add(DatabaseModifyType.CREATE, instance);
        });

        // delete found data
        Reaction.react(type, DatabaseModifyType.DELETE, () -> {
            // data doesn't exist
            if(data.isEmpty()) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }
            boolean result0;

            // if the primaryKey is null then use the filter to delete the value
            // otherwise delete it with the key
            if(primaryKey == null) {
                result0 = module.unset(filter, true, (k, e) -> updatedData.add(DatabaseModifyType.DELETE, k));
            }
            else {
                result0 = module.delete(primaryKey, true);

                updatedData.add(DatabaseModifyType.DELETE, primaryKey);
            }

            // was the deletion successful
            result.set(result0);
        });

        // modify found data
        Reaction.react(type, DatabaseModifyType.MODIFY, () -> {
            // data doesn't exist
            if(data.isEmpty()) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }

            // invalid values
            if(updates == null || !updates.validate()) {
                packet.respond(ResponseStatus.BAD_REQUEST);
                return;
            }

            // the id of every data found
            // and the result value
            Object id;
            boolean result0 = true;

            // loop through data to modify
            for(Object o : data) {
                if(o == null) continue;
                id = ReflectionUtil.getFieldObject(0, o);
                MongoQuery query = updates.toMongoQuery(o);

                // apply updates to raw object
                updates.apply(o, false, false);

                if(updates.has(0)) {
                    // he want to edit the primary key
                    // set to the module with id and modified object / query
                    result0 = module.set(id, updates.get(0).getValue(), o, query.build(), true);

                    updatedData.add(DatabaseModifyType.MODIFY_PRIMARY, StringUtil.join(id, o));
                }
                else {
                    // set to the module with id and modified object / query
                    result0 = module.set(id, o, query.build(), true);

                    // add to updated data
                    updatedData.add(DatabaseModifyType.MODIFY, o);
                }
            }

            // informs the minecraft servers about the update
            result.set(result0);
        });

        // informs the minecraft servers about the update
        updateData(packet, updatedData, dbType, result.get());
    }

    @PacketHandler
    public void onDatabaseModifyRaw(PacketDatabaseModifyNative packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().getDatabaseConnection().isConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }

        // list values
        String database = packet.databaseName;
        DbFilter filter = packet.filter;
        DatabaseModifyType type = packet.type;
        DbQuery updates = packet.updates;
        DatabaseConnection databaseConnection = Cloud.getInstance().getDatabaseConnection();

        // list collection
        MongoCollection<Document> collection = databaseConnection.getCollection(database);
        if(collection == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        Cloud.getInstance().getLogger().debug("Attempting to modify " + database + " database .. (" + type.name() + ")." +
                " With filter (as " + filter + ") and query (as " + updates + ")");

        // list to-edit data
        List<Document> data = databaseConnection.findSync(collection, filter.toBson(), packet.limit).into(new ArrayList<>());

        Cloud.getInstance().getLogger().debug("Found documents(" + data.size() + ") to " + type.name() + ".");

        // create some data
        Reaction.react(type, DatabaseModifyType.CREATE, () -> {
            // data already exist
            if(data.isEmpty()) {
                packet.respond(ResponseStatus.CONFLICT);
                return;
            }

            databaseConnection.insert(collection, updates.toMongoQuery().build());
        });

        // delete found data
        Reaction.react(type, DatabaseModifyType.DELETE, () -> {
            // data doesn't exist
            if(!data.isEmpty()) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }

            long l = databaseConnection.deleteManySync(collection, filter.toBson());
            if(l == 0) {
                // couldn't delete data
                packet.respond(ResponseStatus.NOK);
            }
        });

        // modify found data
        Reaction.react(type, DatabaseModifyType.MODIFY, () -> {
            // data doesn't exist
            if(!data.isEmpty()) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }

            long l = databaseConnection.updateManySync(collection, filter.toBson(), updates.toMongoQuery().build());
            if(l == 0) {
                // couldn't update data
                packet.respond(ResponseStatus.NOK);
            }
        });
        packet.respond(ResponseStatus.OK);
    }

    /**
     * Informs all server about the update
     * The modType (0 = create; 1 = delete; 2 = edit; 3 = edit primkey)
     *
     * @param request       The packets (request)
     * @param type          The type (Group, Player, ..)
     * @param data          The data (e.g. the key or the new data)
     * @param processResult Result (success or not?)
     */
    private void updateData(AbstractPacket request, MultiMap<DatabaseModifyType, Object> data, DatabaseType type, boolean processResult) {
        // update info
        boolean updateDataResult = false;
        for(DatabaseModifyType modifyType : data.keySet()) {
            Set<Object> modifiedData = data.get(modifyType);
            for(Object modifiedDatum : modifiedData) {
                updateDataResult = !updateDataResult && updateData(modifyType, modifiedDatum.toString(), type);
            }
        }

        // trigger update permissions
        request.respond(processResult ? ResponseStatus.OK : ResponseStatus.NOK);
        if(processResult && updateDataResult) {
            PacketMessenger.message(new PacketRequest(PacketRequest.Type.UPDATE_PERM), ClientType.PROXY, ClientType.SERVER);
        }
    }

    /**
     * Updates the data inside the cache
     *
     * @param modifyType The modify type
     * @param data       The data
     * @param type       The database type
     * @return The result (true=updatePerm; false=do not)
     */
    private boolean updateData(DatabaseModifyType modifyType, String data, DatabaseType type) {
        if(type == DatabaseType.GROUP) {
            if(modifyType == DatabaseModifyType.DELETE) {
                MooCache.getInstance().getGroupMap().remove(data);

                // GROUP HAS BEEN DELETED
                return true;
            }
            else if(modifyType == DatabaseModifyType.CREATE || modifyType == DatabaseModifyType.MODIFY) {
                Group group = ReflectionUtil.deserialize(data, Group.class);
                MooCache.getInstance().getGroupMap().fastPut(group.name, group);

                // GROUP HAS BEEN UPDATED
                if(modifyType == DatabaseModifyType.CREATE) {
                    return true;
                }
            }
            else if(modifyType == DatabaseModifyType.MODIFY_PRIMARY) {
                List<String> l = StringUtil.split(data);
                Object id = ReflectionUtil.safeCast(l.get(0));
                Group group = ReflectionUtil.deserialize(l.get(1), Group.class);

                MooCache.getInstance().getGroupMap().remove((String) id);
                MooCache.getInstance().getGroupMap().fastPut(group.name, group);

                return true;
            }
        }
        else if(type == DatabaseType.PLAYER) {
            if(modifyType == DatabaseModifyType.DELETE) {
                MooCache.getInstance().getUniqueIdPlayerMap().remove(UUID.fromString(data));

                // PLAYERDATA HAS BEEN DELETED ? WHAT THE FLACK? (Shouldnt happen)
            }
            else if(modifyType == DatabaseModifyType.CREATE || modifyType == DatabaseModifyType.MODIFY) {
                PlayerData playerData = ReflectionUtil.deserialize(data, PlayerData.class);
                MooCache.getInstance().getUniqueIdPlayerMap().fastPut(playerData.uuid, playerData);

                // UPDATE PERMISSIONS IF EDITED
                if(modifyType == DatabaseModifyType.MODIFY) {
                    return true;
                }
            }
        }
        return false;
    }

}
