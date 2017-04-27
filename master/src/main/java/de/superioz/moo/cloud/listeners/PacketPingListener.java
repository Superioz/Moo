package de.superioz.moo.cloud.listeners;

import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPing;

public class PacketPingListener implements PacketAdapter {

    @PacketHandler
    public void onPing(PacketPing packet) {
        packet.respond(new PacketPing(System.currentTimeMillis()));
    }

}
