package de.superioz.moo.cloud.listeners.player;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.config.MooConfigType;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQueryUnbaked;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.DatabaseCollections;
import de.superioz.moo.cloud.events.MooPlayerJoinedProxyEvent;
import de.superioz.moo.netty.common.ResponseStatus;
import de.superioz.moo.netty.packets.PacketPlayerState;

import java.net.InetSocketAddress;

/**
 * This class listens to a player joining the/a proxy
 */
public class MooPlayerJoinedProxyListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerJoinedProxyEvent event) {
        PlayerData data = event.getData();
        InetSocketAddress clientAddress = event.getClientAddress();
        PacketPlayerState packet = event.getPacket();

        // list the current time
        // set the time as joined time for the player
        long current = System.currentTimeMillis();
        data.setJoined(current);

        // set the new time into the database
        DatabaseCollections.PLAYER.set(data.getUuid(), data, DbQueryUnbaked.newInstance(DbModifier.PLAYER_JOINED, current));

        // sets the server he is currently on
        String serverId = packet.meta;
        int proxyId = Cloud.getInstance().getClientManager().get(clientAddress).getId();
        data.setCurrentServer(serverId);
        data.setCurrentProxy(proxyId);

        // adds player to moo proxy
        Cloud.getInstance().getMooProxy().add(data, clientAddress);

        // set new proxy and server into database
        DatabaseCollections.PLAYER.set(data.getUuid(), data, DbQueryUnbaked.newInstance(DbModifier.PLAYER_PROXY, proxyId));

        // update moo cache (user count and playerData)
        MooCache.getInstance().getConfigMap().fastPutAsync(MooConfigType.PLAYER_COUNT.getKey(), Cloud.getInstance().getMooProxy().getPlayers().size());
        MooCache.getInstance().getUniqueIdPlayerMap().fastPutAsync(data.getUuid(), data);
        MooCache.getInstance().getNameUniqueIdMap().fastPutAsync(data.getLastName(), data.getUuid());

        packet.respond(ResponseStatus.OK);
    }

}