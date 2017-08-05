package de.superioz.moo.cloud.listeners;

import com.mongodb.client.MongoCollection;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.DbFilter;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketDatabaseInfo;
import de.superioz.moo.protocol.packets.PacketDatabaseInfoNative;
import de.superioz.moo.protocol.packets.PacketRespond;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PacketDatabaseInfoListener implements PacketAdapter {

    @PacketHandler
    public void onDatabaseInfo(PacketDatabaseInfo packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().getDatabaseConnection().isConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }

        // get values
        DatabaseType type = packet.databaseType;
        DbFilter filter = packet.filter;

        // what r u doing m8?
        if(type == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        // get the database collection
        DatabaseCollection module = Cloud.getInstance().getDatabaseCollection(type);
        Cloud.getInstance().getLogger().debug("Attempting to fetch data from modules .. (" + type.name() + ")." +
                " With filter (as " + filter + ")");

        // get data from filtering
        List<Object> data = module.getFilteredData(CloudCollections.uniqueIds(), filter, packet.queried, packet.limit);

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
        if(!Cloud.getInstance().getDatabaseConnection().isConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }

        // get values
        String database = packet.databaseName;
        DbFilter filter = packet.filter;
        DatabaseConnection databaseConnection = Cloud.getInstance().getDatabaseConnection();

        // checks if the values are correct
        if(database == null || filter == null || databaseConnection == null) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        // get collection
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
