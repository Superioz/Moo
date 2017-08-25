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
import de.superioz.moo.cloud.events.MooPlayerLeftProxyEvent;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketPlayerState;

public class MooPlayerLeftProxyListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerLeftProxyEvent event) {
        PlayerData data = event.getData();
        PacketPlayerState packet = event.getPacket();

        // list the current times
        long current = System.currentTimeMillis();
        long joined = data.joined;

        // calculate online time
        long onlineTime = 0;
        if(joined > 0 && current > 0 && current > joined) {
            onlineTime = current - data.joined;
        }
        long totalTime = data.totalOnline != null ? data.totalOnline + onlineTime : 0;

        // set the time to the data
        data.lastOnline = current;
        data.joined = 0L;
        data.totalOnline = totalTime;

        // applies the times onto the database
        DbQueryUnbaked query = DbQueryUnbaked
                .newInstance(DbModifier.PLAYER_LAST_ONLINE, current)
                .equate(DbModifier.PLAYER_TOTAL_ONLINE, totalTime)
                .equate(DbModifier.PLAYER_JOINED, 0L)
                .equate(DbModifier.PLAYER_PROXY, -1);
        DatabaseCollections.PLAYER.set(data.uuid, data, query, true);

        // removes player from moo proxy
        Cloud.getInstance().getMooProxy().remove(data.uuid, data.lastName);

        // update user count
        MooCache.getInstance().getConfigMap().fastPutAsync(MooConfigType.PLAYER_COUNT.getKey(),
                Cloud.getInstance().getMooProxy().getPlayers().size());
        packet.respond(ResponseStatus.OK);
    }

}
