package de.superioz.moo.daemon.listeners;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketRespond;
import de.superioz.moo.protocol.packets.PacketServerRequest;
import de.superioz.moo.protocol.packets.PacketServerRequestShutdown;

public class ServerPacketListener implements PacketAdapter {

    @PacketHandler
    public void onServerRequest(PacketServerRequest packet) {
        String serverType = packet.type;

        Daemon.getInstance().getLogs().debug("Got request for " + packet.amount + "x " + serverType + " server(s).");

        // checks if the server type exists
        // if not send NOT_FOUND status
        if(!Daemon.getInstance().getServer().hasPattern(serverType, false)) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        Daemon.getInstance().getLogs().debug("Starting server ..");

        // adds given amount of server to start queue
        Daemon.getInstance().startServer(serverType, "", packet.autoSave, packet.amount,
                server -> {
                    packet.respond(server == null
                            ? new PacketRespond(ResponseStatus.NOK)
                            : new PacketRespond(ResponseStatus.OK));
                }
        );
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet) {
        Server server = Daemon.getInstance().getServer().getServer(packet.port);

        // if the server was not found
        if(server == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // if the server had an error while stopping
        packet.respond(server.stop() ? ResponseStatus.OK : ResponseStatus.NOK);
    }

}
