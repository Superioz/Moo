package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.common.ResponseStatus;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.*;
import de.superioz.moo.netty.server.MooProxy;

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
        ServerPattern pattern = MooCache.getInstance().getPatternMap().get(type);
        if(pattern == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // check if the amount is too high! (over 9000!)
        int maxServers = pattern.getMax();
        int current = MooProxy.getInstance().getServer(type).size();
        if(current == maxServers || (current + packet.amount) >= maxServers) {
            packet.respond(ResponseStatus.BAD_REQUEST);
            return;
        }

        Cloud.getInstance().getMooProxy().requestServer(type, packet.autoSave, packet.amount, abstractPacket -> packet.respond(abstractPacket));
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet) {
        // does a server exist with this?
        if(MooProxy.getInstance().getServer(packet.host, packet.port) == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        // shutdowns a server
        Cloud.getInstance().getMooProxy().requestServerShutdown(packet.host, packet.port, abstractPacket -> packet.respond(abstractPacket));
    }

}
