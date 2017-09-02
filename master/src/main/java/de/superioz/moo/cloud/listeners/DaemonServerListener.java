package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketRamUsage;
import de.superioz.moo.network.packets.PacketServerAttempt;
import de.superioz.moo.network.packets.PacketServerRequest;
import de.superioz.moo.network.packets.PacketServerRequestShutdown;
import de.superioz.moo.network.server.MooProxy;

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
        String type = packet.type;

        // exists the type?
        ServerPattern pattern = MooProxy.getInstance().getPattern(type);
        if(pattern == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // check if the amount is too high! (over 9000!)
        int maxServers = pattern.getMax();
        int current = MooProxy.getInstance().getServers(type).size();
        if(current == maxServers || (current + packet.amount) >= maxServers) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        Cloud.getInstance().getNetworkProxy().requestServer(type, packet.autoSave, packet.amount, abstractPacket -> packet.respond(abstractPacket));
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet) {
        // does a server exist with this?
        if(MooProxy.getInstance().getServer(packet.host, packet.port) == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // shutdowns a server
        Cloud.getInstance().getNetworkProxy().requestServerShutdown(packet.host, packet.port, abstractPacket -> packet.respond(abstractPacket));
    }

}
