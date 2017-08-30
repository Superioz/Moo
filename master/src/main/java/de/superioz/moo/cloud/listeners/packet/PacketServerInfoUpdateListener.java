package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketServerInfoUpdate;
import de.superioz.moo.netty.server.MooProxy;

import java.net.InetSocketAddress;

public class PacketServerInfoUpdateListener implements PacketAdapter {

    @PacketHandler
    public void onServerUpdate(PacketServerInfoUpdate packet) {
        InetSocketAddress address = packet.serverAddress;

        // list the server registered and check if it exists
        MooServer server = Cloud.getInstance().getNetworkProxy().getServer(address);
        if(server == null) {
            // it does not exist? LUL
            return;
        }

        // updates the server info
        server.updateInfo(packet.motd, packet.onlinePlayers, packet.maxPlayers);

        // updates server cycle
        MooProxy.getInstance().serverCycle(server.getPattern());
    }

}
