package de.superioz.moo.proxy.listeners;

import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketServerRegister;
import de.superioz.moo.network.packets.PacketServerUnregister;
import de.superioz.moo.proxy.Thunder;

public class ServerRegisterChangeListener implements PacketAdapter {

    @PacketHandler
    public void onServerRegister(PacketServerRegister packet) {
        // register server
        Thunder.getInstance().registerServer(packet.type, packet.host, packet.id, packet.port, "", false);
    }

    @PacketHandler
    public void onServerUnregister(PacketServerUnregister packet) {
        // unregister server
        Thunder.getInstance().unregisterServer(packet.address.getHostName(), packet.address.getPort());
    }

}
