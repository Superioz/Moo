package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
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

        // get the player and check if exists
        PlayerData player = MooCache.getInstance().getUniqueIdPlayerMap().get(data.uuid);
        if(player == null){
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // otherwise change currentServer
        player.currentServer = packet.meta;
        packet.respond(ResponseStatus.OK);

        // update Moo proxy
        Cloud.getInstance().getMooProxy().getPlayerMap().put(player.uuid, player);
        Cloud.getInstance().getMooProxy().getPlayerNameMap().put(player.lastName, player);

        // update data of player in database and cache
        CloudCollections.PLAYER.set(data.uuid, data,
                DbQueryUnbaked.newInstance(DbModifier.PLAYER_SERVER, packet.meta), true);
        MooCache.getInstance().getUniqueIdPlayerMap().fastPutAsync(data.uuid, player);
    }

}
