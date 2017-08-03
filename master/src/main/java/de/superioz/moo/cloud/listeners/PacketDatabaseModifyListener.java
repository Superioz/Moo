package de.superioz.moo.cloud.listeners;

import com.mongodb.client.MongoCollection;
import de.superioz.moo.api.collection.Tuple;
import de.superioz.moo.api.database.*;
import de.superioz.moo.api.keyvalue.FinalValue;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketDatabaseModify;
import de.superioz.moo.protocol.packets.PacketDatabaseModifyNative;
import de.superioz.moo.protocol.packets.PacketRespond;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

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

        // get collection
        DatabaseCollection module = Cloud.getInstance().getDatabaseCollection(dbType);
        Cloud.getLogger().debug("Attempting to modify " + dbType.name() + " modules .. (" + type.name() + ")." +
                " With filter (as " + filter + ") and query (as " + updates + ")");

        // primary key of the filter
        DbFilterNode firstNode = realFilter.getKey(0, module.getWrappedClass());
        Object primaryKey = firstNode == null ? null : firstNode.getContent();

        // get data from filtering
        List<Object> data = module.getFilteredData(CloudCollections.uniqueIds(), filter, packet.queried, packet.limit);
        Cloud.getLogger().debug("Found data(" + data.size() + ") to " + type.name() + ": " + data);

        // for informing minecraft
        FinalValue<Boolean> result = new FinalValue<>(true);
        Tuple<Object> newData = new Tuple();

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
            newData.add(PacketDatabaseModify.MODIFY_CREATE, instance);
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
                result0 = module.unset(filter, true, (k, e) -> newData.add(PacketDatabaseModify.MODIFY_DELETE, k));
            }
            else {
                result0 = module.delete(primaryKey, true);

                newData.add(PacketDatabaseModify.MODIFY_DELETE, primaryKey);
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

                    newData.add(PacketDatabaseModify.MODIFY_PRIMARY, StringUtil.join(id, o));
                }
                else {
                    // set to the module with id and modified object / query
                    result0 = module.set(id, o, query.build(), true);

                    // add to updated data
                    newData.add(PacketDatabaseModify.MODIFY_MODIFY, o);
                }
            }

            // informs the minecraft servers about the update
            result.set(result0);
        });

        // informs the minecraft servers about the update
        informMinecraft(packet, newData.toList(objects -> String.join(PacketDatabaseModify.MODIFY_SEPERATOR, StringUtil.toStringList(objects))),
                dbType, result.get());
    }

    @PacketHandler
    public void onDatabaseModifyRaw(PacketDatabaseModifyNative packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().getDatabaseConnection().isConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }

        // get values
        String database = packet.databaseName;
        DbFilter filter = packet.filter;
        DatabaseModifyType type = packet.type;
        DbQuery updates = packet.updates;
        DatabaseConnection databaseConnection = Cloud.getInstance().getDatabaseConnection();

        // get collection
        MongoCollection<Document> collection = databaseConnection.getCollection(database);
        if(collection == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        Cloud.getLogger().debug("Attempting to modify " + database + " database .. (" + type.name() + ")." +
                " With filter (as " + filter + ") and query (as " + updates + ")");

        // get to-edit data
        List<Document> data = databaseConnection.findSync(collection, filter.toBson(), packet.limit).into(new ArrayList<>());

        Cloud.getLogger().debug("Found documents(" + data.size() + ") to " + type.name() + ".");

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
     * @param req           The packets (request)
     * @param type          The type (Group, Player, ..)
     * @param data          The data (e.g. the key or the new data)
     * @param processResult Result (success or not?)
     */
    private void informMinecraft(AbstractPacket req, List<String> data, DatabaseType type, boolean processResult) {
        // create respond
        PacketRespond respond = new PacketRespond(PacketRespond.MODIFICATION_PREFIX + type.name().toLowerCase(), data,
                processResult ? ResponseStatus.OK : ResponseStatus.NOK);
        //respond.setUniqueId(req.getUniqueId());

        //.
        Cloud.getLogger().debug("Inform servers about " + type.name()
                + " database modification(" + data.size() + "): " + data);

        // send respond
        req.respond(processResult ? ResponseStatus.OK : ResponseStatus.NOK);
        if(processResult) {
            PacketMessenger.message(respond, ClientType.PROXY, ClientType.SERVER);
        }
    }

}
