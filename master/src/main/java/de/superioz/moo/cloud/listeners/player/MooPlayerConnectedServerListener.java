package de.superioz.moo.cloud.listeners.player;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.redis.MooCache;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.MooPlayerConnectedServerEvent;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.network.packets.PacketPlayerState;

/**
 * This class listens on a player being connected to a server
 */
public class MooPlayerConnectedServerListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerConnectedServerEvent event) {
        PlayerData data = event.getData();
        PacketPlayerState packet = event.getPacket();

        // list the player and check if exists
        PlayerData player = MooCache.getInstance().getPlayerMap().get(data.getUuid());
        if(player == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // otherwise change currentServer
        player.setCurrentServer(packet.meta);
        packet.respond(ResponseStatus.OK);

        // update Moo proxy and cache
        Cloud.getInstance().getNetworkProxy().getPlayerMap().put(player.getUuid(), player);
        Cloud.getInstance().getNetworkProxy().getPlayerNameMap().put(player.getLastName(), player);
        MooCache.getInstance().getPlayerMap().fastPutAsync(data.getUuid(), player);
    }

}
