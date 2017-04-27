package de.superioz.moo.cloud.listeners;

import com.mongodb.client.FindIterable;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketDatabaseCount;
import de.superioz.moo.protocol.packets.PacketRespond;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class PacketDatabaseCountListener implements PacketAdapter {

    @PacketHandler
    public void onDatabaseCount(PacketDatabaseCount packet) {
        // checks if the database is connected
        if(!Cloud.getInstance().getDatabaseConnection().isConnected()) {
            packet.respond(ResponseStatus.NO_DATABASE);
            return;
        }
        DatabaseType type = packet.databaseType;
        int mode = packet.mode;

        // gets the database collection and if the database collection is not found
        // throw a critically response status
        DatabaseCollection module = Cloud.getInstance().getDatabaseCollection(type);
        if(module == null) {
            packet.respond(ResponseStatus.INTERNAL_ERROR);
            return;
        }

        // just count the entries and return an integer
        boolean countMode = mode == 0;
        Reaction.react(countMode, () -> {
            packet.respond(new PacketRespond(type.name().toLowerCase(), module.count(null) + "", ResponseStatus.OK));
        });

        // list entries from collection
        Reaction.react(!countMode, () -> {
            FindIterable<Document> documents = module.fetch(null, packet.limit);
            List<Object> l = new ArrayList<>();

            for(Document d : documents) {
                Object o = module.convert(d);
                l.add(o);
            }

            packet.respond(new PacketRespond(type.name().toLowerCase(), StringUtil.toStringList(l), ResponseStatus.OK));
        });
    }

}
