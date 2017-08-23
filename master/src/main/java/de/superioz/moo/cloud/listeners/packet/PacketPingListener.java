package de.superioz.moo.cloud.listeners.packet;

import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketPing;

/**
 * This class listens to PacketPing
 */
public class PacketPingListener implements PacketAdapter {

    @PacketHandler
    public void onPing(PacketPing packet) {
        packet.respond(new PacketPing(System.currentTimeMillis()));
    }

}
