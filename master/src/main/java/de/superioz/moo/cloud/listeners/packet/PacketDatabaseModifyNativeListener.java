package de.superioz.moo.cloud.listeners.packet;

import com.mongodb.client.MongoCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseModifyType;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketDatabaseModifyNative;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * This class listens to PacketDatabaseModifyNative
 *
 * @see PacketDatabaseModifyNative
 */
public class PacketDatabaseModifyNativeListener implements PacketAdapter {

    @PacketHandler
    public void onDatabaseModifyRaw(PacketDatabaseModifyNative packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().isDatabaseConnected()) {
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

        // list to-edit data
        List<Document> data = databaseConnection.findSync(collection, filter.toBson(), packet.limit).into(new ArrayList<>());

        // CREATE some data
        Reaction.react(type, DatabaseModifyType.CREATE, () -> {
            // data already exist
            if(data.isEmpty()) {
                packet.respond(ResponseStatus.CONFLICT);
                return;
            }

            databaseConnection.insert(collection, updates.toMongoQuery().build());
        });
        // DELETE found data
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
        // MODIFY found data
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

}
