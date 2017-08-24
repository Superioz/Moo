package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.collection.MultiMap;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseModifyType;
import de.superioz.moo.api.database.DatabaseType;
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
import de.superioz.moo.protocol.packets.PacketUpdatePermission;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * This class listens to PacketDatabaseModify
 *
 * @see PacketDatabaseModify
 */
public class PacketDatabaseModifyListener implements PacketAdapter {

    @PacketHandler
    public void onDatabaseModify(PacketDatabaseModify packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().isDatabaseConnected()) {
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

        // primary key of the filter
        DbFilterNode firstNode = realFilter.getKey(0, module.getWrappedClass());
        Object primaryKey = firstNode == null ? null : firstNode.getContent();

        // list data from filtering
        List<Object> data = module.getFilteredData(DatabaseCollections.PLAYER, filter, packet.queried, packet.limit);

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
            boolean subResult = true;

            // loop through data to modify
            for(Object subData : data) {
                if(subData == null) continue;

                // get primary key of subdata
                id = ReflectionUtil.getFieldObject(0, subData);
                MongoQuery query = updates.toMongoQuery(subData);

                // apply updates to raw object
                updates.apply(subData, false, false);

                if(updates.has(0)) {
                    // he want to edit the primary key
                    // set to the module with id and modified object / query
                    subResult = module.set(id, updates.get(0).getValue(), subData, query.build(), true);

                    updatedData.add(DatabaseModifyType.MODIFY_PRIMARY, StringUtil.join(id, subData));
                }
                else {
                    // set to the module with id and modified object / query
                    subResult = module.set(id, subData, query.build(), true);

                    // add to updated data
                    updatedData.add(DatabaseModifyType.MODIFY, subData);
                }
            }

            // informs the minecraft servers about the update
            result.set(subResult);
        });

        // updates the REDIS CACHE data
        updateData(packet, primaryKey, updatedData, dbType, result.get());
    }

    /**
     * Gets every updated data and informs the cache about the updated-
     *
     * @param request       The packets (request)
     * @param key           The key
     * @param type          The type (Group, Player, ..)
     * @param data          The data (e.g. the key or the new data)
     * @param processResult Result (success or not?)
     * @see #updateData(DatabaseModifyType, String, DatabaseType)
     */
    private void updateData(AbstractPacket request, Object key, MultiMap<DatabaseModifyType, Object> data, DatabaseType type, boolean processResult) {
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
            PacketMessenger.message(new PacketUpdatePermission(type, key + ""), ClientType.PROXY);
        }
    }

    /**
     * Similar to {@link #updateData(AbstractPacket, Object, MultiMap, DatabaseType, boolean)} but with only subdata not a whole set
     * of updates.
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
