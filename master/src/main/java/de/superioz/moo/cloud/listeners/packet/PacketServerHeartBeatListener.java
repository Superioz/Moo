package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.network.common.MooServer;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketServerHeartBeat;

public class PacketServerHeartBeatListener implements PacketAdapter {

    @PacketHandler
    public void onServerHeartBeat(PacketServerHeartBeat packet) {
        MooServer server = Cloud.getInstance().getNetworkProxy().getServer(packet.serverAddress);

        if(server != null) server.heartbeat();
    }

}
