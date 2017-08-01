package de.superioz.moo.proxy.listeners;

import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketServerInfoUpdate;
import de.superioz.moo.proxy.common.ServerCache;
import de.superioz.moo.proxy.common.ServerSpecificInfo;

public class PacketServerUpdateListener implements PacketAdapter {

    @PacketHandler
    public void onServerInfo(PacketServerInfoUpdate packet) {
        ServerCache.getInstance().updateServer(packet.serverAddress,
                new ServerSpecificInfo(packet.motd, packet.onlinePlayers, packet.maxPlayers));
    }

}
