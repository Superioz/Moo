package de.superioz.moo.proxy.listeners;

import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketServerRegister;
import de.superioz.moo.netty.packets.PacketServerUnregister;
import de.superioz.moo.proxy.Thunder;

public class ServerRegisterChangeListener implements PacketAdapter {

    @PacketHandler
    public void onServerRegister(PacketServerRegister packet) {
        Thunder.getInstance().registerServer(packet.type, packet.host, packet.port);
    }

    @PacketHandler
    public void onServerUnregister(PacketServerUnregister packet) {
        Thunder.getInstance().unregisterServer(packet.address.getHostName(), packet.address.getPort());
    }

}
