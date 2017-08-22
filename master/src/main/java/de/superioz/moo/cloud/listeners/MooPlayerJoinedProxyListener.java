package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.query.DbQueryUnbaked;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.io.MooConfigType;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.cloud.events.MooPlayerJoinedProxyEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketPlayerState;

import java.net.InetSocketAddress;

public class MooPlayerJoinedProxyListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerJoinedProxyEvent event) {
        PlayerData data = event.getData();
        InetSocketAddress clientAddress = event.getClientAddress();
        PacketPlayerState packet = event.getPacket();

        // get the current time
        // set the time as joined time for the player
        long current = System.currentTimeMillis();
        data.joined = current;

        // set the new time into the database
        CloudCollections.PLAYER.set(data.uuid, data,
                DbQueryUnbaked.newInstance(DbModifier.PLAYER_JOINED, current), true);

        // get group
        // check maintenance
        Group group = CloudCollections.GROUP.get(data.group);
        if((boolean) Cloud.getInstance().getConfig().get("minecraft.maintenance")
                && (group.rank < ((Integer) Cloud.getInstance().getConfig().get("minecraft.maintenance-rank")))) {
            // if maintenance is true and rank is too low
            packet.respond(ResponseStatus.NOK);
            return;
        }

        // sets the server he is currently on
        String serverId = packet.meta;
        int proxyId = Cloud.getInstance().getClientManager().get(clientAddress).getId();
        data.setCurrentServer(serverId);
        data.setCurrentProxy(proxyId);

        // adds player to moo proxy
        Cloud.getInstance().getMooProxy().add(data, clientAddress);

        // set new proxy and server into database
        CloudCollections.PLAYER.set(data.uuid, data,
                DbQueryUnbaked.newInstance(DbModifier.PLAYER_PROXY, proxyId), true);

        // update moo cache (user count and playerData)
        MooCache.getInstance().getConfigMap().fastPutAsync(MooConfigType.PLAYER_COUNT.getKey(),
                Cloud.getInstance().getMooProxy().getPlayers().size());
        MooCache.getInstance().getUniqueIdPlayerMap().fastPutAsync(data.uuid, data)
                .thenAccept(aBoolean -> packet.respond(ResponseStatus.OK));
        MooCache.getInstance().getNameUniqueIdMap().fastPutAsync(data.lastName, data.uuid);
    }

}