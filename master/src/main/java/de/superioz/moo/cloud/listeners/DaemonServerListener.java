package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.*;

import java.net.InetSocketAddress;

public class DaemonServerListener implements PacketAdapter {

    @PacketHandler
    public void onServerAttempt(PacketServerAttempt packet) {
        Cloud.getInstance().getLogger().debug("Daemon attempted to " + packet.type.name() + " a server. [" + packet.id + "]");
    }

    @PacketHandler
    public void onServerDone(PacketServerDone packet) {
        String ip = packet.getAddress().getHostName() + ":" + packet.port;
        PacketMessenger.message(packet, ClientType.PROXY);

        // daemon started a server
        boolean startedServer = packet.doneType == PacketServerDone.Type.START;
        Reaction.react(startedServer, () -> {
            Cloud.getInstance().getLogger().debug("Register server " + ip + " with type '" + packet.type + "' ..");

            PacketMessenger.message(new PacketServerRegister(packet.type, packet.getAddress().getHostName(), packet.port), ClientType.PROXY);
            MooServer server = new MooServer(packet.uuid, new InetSocketAddress(packet.getAddress().getHostName(), packet.port), packet.type);
            Cloud.getInstance().getMooProxy().getSpigotServers().put(packet.uuid, server);

            // sync with redis
            MooCache.getInstance().getServerMap().putAsync(server.getUuid(), server);
        });

        // daemon stopped a server
        Reaction.react(!startedServer, () -> {
            // check if the spigot server exists anyway
            if(Cloud.getInstance().getMooProxy().getSpigotServers().containsKey(packet.uuid)) {
                Cloud.getInstance().getLogger().debug("Unregister server " + ip + " with type '" + packet.type + "' ..");

                PacketMessenger.message(new PacketServerUnregister(packet.getAddress()), ClientType.PROXY);
                Cloud.getInstance().getMooProxy().getSpigotServers().remove(packet.uuid);

                // sync with redis
                MooCache.getInstance().getServerMap().removeAsync(packet.uuid);
            }
        });
    }

    @PacketHandler
    public void onRamUsage(PacketRamUsage packet) {
        //Cloud.getInstance().getLogger().debug("Updates ram usage for " + packet.getChannel().remoteAddress() + " (to " + packet.ramUsage + "%)");

        Cloud.getInstance().getClientManager().updateRamUsage((InetSocketAddress) packet.getChannel().remoteAddress(), packet.ramUsage);
    }

    @PacketHandler
    public void onServerRequest(PacketServerRequest packet) {
        Cloud.getInstance().getMooProxy().requestServer(packet.type, packet.autoSave, packet.amount, abstractPacket -> packet.respond(abstractPacket));
    }

    @PacketHandler
    public void onServerRequestShutdown(PacketServerRequestShutdown packet) {
        Cloud.getInstance().getMooProxy().requestServerShutdown(packet.host, packet.port, abstractPacket -> packet.respond(abstractPacket));
    }

}
