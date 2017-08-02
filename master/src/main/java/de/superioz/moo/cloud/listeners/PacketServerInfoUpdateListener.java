package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketServerInfoUpdate;

import java.net.InetSocketAddress;

public class PacketServerInfoUpdateListener implements PacketAdapter {

    @PacketHandler
    public void onServerUpdate(PacketServerInfoUpdate packet) {
        InetSocketAddress address = packet.serverAddress;

        // get the server registered and check if it exists
        MooServer server = Cloud.getInstance().getMooProxy().getServer(address);
        if(server == null) {
            // it does not exist? LUL
            return;
        }

        // updates the server info
        server.updateInfo(packet.motd, packet.onlinePlayers, packet.maxPlayers);
    }

}
