package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooPlayer;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.DbQueryUnbaked;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.database.CloudCollections;
import de.superioz.moo.cloud.events.MooPlayerJoinedProxyEvent;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketConfig;
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
        CloudCollections.players().set(data.uuid, data,
                DbQueryUnbaked.newInstance(DbModifier.PLAYER_JOINED, current), true);

        // get group
        // check maintenance
        Group group = CloudCollections.groups().get(data.group);
        if((boolean) Cloud.getInstance().getConfig().get("minecraft.maintenance")
                && (group.rank < ((Integer) Cloud.getInstance().getConfig().get("minecraft.maintenance-rank")))) {
            // if maintenance is true and rank is too low
            packet.respond(ResponseStatus.NOK);
            return;
        }

        // sets the server he is currently on
        String serverId = packet.meta;
        int proxyId = Cloud.getInstance().getHub().get(clientAddress).getId();
        Cloud.getInstance().getMooProxy().add(new MooPlayer(data.uuid, data.lastName, data.lastip, proxyId, serverId), clientAddress);

        // update user count
        PacketMessenger.message(new PacketConfig(PacketConfig.Command.CHANGE, PacketConfig.Type.PLAYER_COUNT,
                Cloud.getInstance().getMooProxy().getPlayers().size() + ""), ClientType.PROXY);
    }

}