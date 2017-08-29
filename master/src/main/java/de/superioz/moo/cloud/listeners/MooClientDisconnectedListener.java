package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.MooServerRestockEvent;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.events.MooClientDisconnectEvent;
import de.superioz.moo.netty.packets.PacketServerUnregister;
import de.superioz.moo.netty.server.MooClient;
import de.superioz.moo.netty.server.MooProxy;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class listens on a client disconnecting from the cloud
 */
public class MooClientDisconnectedListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMooClientDisconnect(MooClientDisconnectEvent event) {
        MooClient client = event.getClient();

        // if the moo client disconnects ..
        // BUNGEE BUNGEE BUNGEE if the type is PROXY
        if(client.getType() == ClientType.PROXY) {
            List<UUID> toRemove = new ArrayList<>();
            for(UUID uuid : Cloud.getInstance().getNetworkProxy().getPlayerServerMap().keySet()) {
                InetSocketAddress proxyAddress = Cloud.getInstance().getNetworkProxy().getPlayerServerMap().get(uuid);
                if(proxyAddress.equals(client.getAddress())) toRemove.add(uuid);
            }
            toRemove.forEach(uuid -> Cloud.getInstance().getNetworkProxy().getPlayerServerMap().remove(uuid));

            // update player count
            MooCache.getInstance().getConfigMap().fastPutAsync(NetworkConfigType.PLAYER_COUNT.getKey(),
                    Cloud.getInstance().getNetworkProxy().getPlayers().size());
        }
        // SPIGOT SPIGOT SPIGOT ouh, the server went down, let's just unregister the server
        else if(client.getType() == ClientType.SERVER) {
            String ip = client.getAddress().getHostName() + ":" + client.getSubPort();
            Cloud.getInstance().getLogger().debug("Unregister server " + ip + " with type '" + client.getName() + "' ..");

            // unregister server
            Cloud.getInstance().getNetworkProxy().unregisterServer(client);

            // call event for new server start
            ServerPattern pattern = MooProxy.getInstance().getPattern(client.getName());
            if(pattern != null) {
                EventExecutor.getInstance().execute(new MooServerRestockEvent(pattern));
            }

            // informing the PROXY!
            PacketMessenger.message(new PacketServerUnregister(client.getAddress()), ClientType.PROXY);
        }
    }

}
