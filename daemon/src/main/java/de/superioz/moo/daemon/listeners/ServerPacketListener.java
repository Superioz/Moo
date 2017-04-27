package de.superioz.moo.daemon.listeners;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.util.Ports;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.task.ServerStartTask;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketServerRequest;
import de.superioz.moo.protocol.packets.PacketServerRequestShutdown;

public class ServerPacketListener implements PacketAdapter {

    @PacketHandler
    public void onServerRequest(PacketServerRequest packet){
        for(int i = 0; i < packet.amount; i++) {
            ServerStartTask task = new ServerStartTask(packet.type, Ports.getAvailablePort(), packet.autoSave);
            Daemon.server.getServerQueue().getQueue().add(task);
        }
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet){
        Server server = Daemon.server.getServer(packet.port);

        if(server != null){
            server.stop();
        }
    }

}
