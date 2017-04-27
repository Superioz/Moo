package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.DbQueryUnbaked;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.cloud.events.MooPlayerLeftProxyEvent;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketConfig;
import de.superioz.moo.protocol.packets.PacketPlayerState;

import java.net.InetSocketAddress;

public class MooPlayerLeftProxyListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerLeftProxyEvent event) {
        PlayerData data = event.getData();
        InetSocketAddress clientAddress = event.getClientAddress();
        PacketPlayerState packet = event.getPacket();

        // get the current times
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
                .append(DbModifier.PLAYER_TOTAL_ONLINE, totalTime)
                .append(DbModifier.PLAYER_JOINED, 0L);
        CloudCollections.players().set(data.uuid, data, query, true);

        // removes player from moo proxy
        Cloud.getInstance().getMooProxy().remove(data.uuid, data.lastName);

        // update user count
        PacketMessenger.message(new PacketConfig(PacketConfig.Command.CHANGE, PacketConfig.Type.PLAYER_COUNT,
                Cloud.getInstance().getMooProxy().getPlayers().size() + ""), ClientType.PROXY);
    }

}
