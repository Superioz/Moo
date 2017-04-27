package de.superioz.moo.proxy.listeners;

import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketServerRegister;
import de.superioz.moo.protocol.packets.PacketServerUnregister;
import de.superioz.moo.proxy.Thunder;

public class ServerRegisterChangeListener implements PacketAdapter {

    @PacketHandler
    public void onServerRegister(PacketServerRegister packet) {
        Thunder.registerServer(packet.type, packet.getAddress().getHostName(), packet.port);
    }

    @PacketHandler
    public void onServerUnregister(PacketServerUnregister packet) {
        Thunder.unregisterServer(packet.getAddress().getHostName(), packet.port);
    }

}
