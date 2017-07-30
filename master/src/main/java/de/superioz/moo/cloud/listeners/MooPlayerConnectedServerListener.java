package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.DbQueryUnbaked;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.cloud.events.MooPlayerConnectedServerEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketPlayerState;

public class MooPlayerConnectedServerListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerConnectedServerEvent event) {
        PlayerData data = event.getData();
        PacketPlayerState packet = event.getPacket();

        PlayerData player = Cloud.getInstance().getMooProxy().getPlayer(data.uuid);

        if(player != null) {
            player.currentServer = packet.meta;
            packet.respond(ResponseStatus.OK);

            // change server in database
            CloudCollections.players().set(data.uuid, data,
                    DbQueryUnbaked.newInstance(DbModifier.PLAYER_SERVER, packet.meta), true);
        }
        else {
            packet.respond(ResponseStatus.NOT_FOUND);
        }
    }

}
