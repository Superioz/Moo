package de.superioz.moo.cloud.listeners;

import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketRamUsage;
import de.superioz.moo.protocol.packets.PacketServerAttempt;
import de.superioz.moo.protocol.packets.PacketServerRequest;
import de.superioz.moo.protocol.packets.PacketServerRequestShutdown;

import java.net.InetSocketAddress;

/**
 * This class listens on server status events of the daemon (starting, stopping, requesting, ..)
 */
public class DaemonServerListener implements PacketAdapter {

    @PacketHandler
    public void onServerAttempt(PacketServerAttempt packet) {
        // attempting to do something! YEAH!
        Cloud.getInstance().getLogger().debug("Daemon attempted to " + packet.type.name() + " a server. [" + packet.id + "]");
    }

    @PacketHandler
    public void onRamUsage(PacketRamUsage packet) {
        //Cloud.getInstance().getLogger().debug("Updates ram usage for " + packet.getChannel().remoteAddress() + " (to " + packet.ramUsage + "%)");

        // updates ram usage
        Cloud.getInstance().getClientManager().updateRamUsage((InetSocketAddress) packet.getChannel().remoteAddress(), packet.ramUsage);
    }

    @PacketHandler
    public void onServerRequest(PacketServerRequest packet) {
        // requests a server
        Cloud.getInstance().getMooProxy().requestServer(packet.type, packet.autoSave, packet.amount, abstractPacket -> packet.respond(abstractPacket));
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet) {
        // shutdowns a server
        Cloud.getInstance().getMooProxy().requestServerShutdown(packet.host, packet.port, abstractPacket -> packet.respond(abstractPacket));
    }

}
