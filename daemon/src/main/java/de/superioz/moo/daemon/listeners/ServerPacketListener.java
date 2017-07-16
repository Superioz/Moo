package de.superioz.moo.daemon.listeners;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.util.Ports;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.task.ServerStartTask;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketServerRequest;
import de.superioz.moo.protocol.packets.PacketServerRequestShutdown;

public class ServerPacketListener implements PacketAdapter {

    @PacketHandler
    public void onServerRequest(PacketServerRequest packet) {
        String serverType = packet.type;

        // checks if the server type exists
        // if not send NOT_FOUND status
        if(!Daemon.server.hasPattern(serverType, false)) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // adds given amount of server to start queue
        for(int i = 0; i < packet.amount; i++) {
            ServerStartTask task = new ServerStartTask(serverType, Ports.getAvailablePort(), packet.autoSave);
            Daemon.server.getServerQueue().getQueue().add(task);
        }
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet) {
        Server server = Daemon.server.getServer(packet.port);

        // if the server was not found
        if(server == null){
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // if the server had an error while stopping
        if(!server.stop()) {
            packet.respond(ResponseStatus.INTERNAL_ERROR);
        }
    }

}
