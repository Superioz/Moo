package de.superioz.moo.cloud.listeners.packet;

import com.mongodb.client.MongoCollection;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketDatabaseInfo;
import de.superioz.moo.network.packets.PacketDatabaseInfoNative;
import de.superioz.moo.network.packets.PacketRespond;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PacketDatabaseInfoListener implements PacketAdapter {

    @PacketHandler
    public void onDatabaseInfo(PacketDatabaseInfo packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().isDatabaseConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }

        // list values
        DatabaseType type = packet.databaseType;
        DbFilter filter = packet.filter;

        // what r u doing m8?
        if(type == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        // list the database collection
        DatabaseCollection module = Cloud.getInstance().getDatabaseCollection(type);
        Cloud.getInstance().getLogger().debug("Attempting to fetch data from modules .. (" + type.name() + ")." +
                " With filter (as " + filter + ")");

        // list data from filtering
        List<Object> data = module.getFilteredData(DatabaseCollections.PLAYER, filter, packet.queried, packet.limit);

        // check result
        if(data.isEmpty()) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }
        packet.respond(new PacketRespond(type.name().toLowerCase(), StringUtil.toStringList(data), ResponseStatus.OK));
    }

    @PacketHandler
    public void onDatabaseInfoRaw(PacketDatabaseInfoNative packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().isDatabaseConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }

        // list values
        String database = packet.databaseName;
        DbFilter filter = packet.filter;
        DatabaseConnection databaseConnection = Cloud.getInstance().getDatabaseConnection();

        // checks if the values are correct
        if(database == null || filter == null || databaseConnection == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        // list collection
        MongoCollection<Document> collection = databaseConnection.getCollection(database);
        if(collection == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        Cloud.getInstance().getLogger().debug("Attempting to fetch data from explicit database .. (" + database + ")." +
                " With filter (as " + filter + ")");

        // find entries and send them
        databaseConnection.find(collection, filter.toBson(), packet.limit, documents -> {
            List<String> l = new ArrayList<>();
            documents.forEach((Consumer<Document>) document -> l.add(document.toJson()));

            if(l.isEmpty()) {
                packet.respond(ResponseStatus.NOT_FOUND);
                return;
            }
            packet.respond(new PacketRespond(database, l, ResponseStatus.OK));
        });
    }

}
