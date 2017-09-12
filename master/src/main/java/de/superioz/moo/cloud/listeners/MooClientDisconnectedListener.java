package de.superioz.moo.cloud.listeners;

import de.superioz.moo.network.common.MooCache;
import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.client.ClientType;
import de.superioz.moo.network.common.PacketMessenger;
import de.superioz.moo.network.events.MooClientDisconnectEvent;
import de.superioz.moo.network.packets.PacketServerUnregister;
import de.superioz.moo.network.client.MooClient;
import de.superioz.moo.network.common.MooProxy;

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
            ServerPattern pattern = MooProxy.getPattern(client.getName());
            MooProxy.serverCycle(pattern);

            // Informing the PROXY!
            PacketMessenger.message(new PacketServerUnregister(client.getAddress()), ClientType.PROXY);
        }
    }

}
