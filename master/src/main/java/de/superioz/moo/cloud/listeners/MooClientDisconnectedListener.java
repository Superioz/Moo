package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.config.MooConfigType;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.events.MooClientDisconnectEvent;
import de.superioz.moo.protocol.packets.PacketServerUnregister;
import de.superioz.moo.protocol.server.MooClient;

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
            for(UUID uuid : Cloud.getInstance().getMooProxy().getPlayerServerMap().keySet()) {
                InetSocketAddress address = Cloud.getInstance().getMooProxy().getPlayerServerMap().get(uuid);
                if(address.equals(client.getAddress())) toRemove.add(uuid);
            }

            toRemove.forEach(uuid -> Cloud.getInstance().getMooProxy().getPlayerServerMap().remove(uuid));

            // update player count
            MooCache.getInstance().getConfigMap().fastPutAsync(MooConfigType.PLAYER_COUNT.getKey(),
                    Cloud.getInstance().getMooProxy().getPlayers().size());
        }
        // SPIGOT SPIGOT SPIGOT ouh, the server went down, let's just unregister the server
        else if(client.getType() == ClientType.SERVER) {
            String ip = client.getAddress().getHostName() + ":" + client.getSubPort();
            Cloud.getInstance().getLogger().debug("Unregister server " + ip + " with type '" + client.getName() + "' ..");

            // unregister server
            Cloud.getInstance().getMooProxy().unregisterServer(client);

            // informing the PROXY!
            PacketMessenger.message(new PacketServerUnregister(client.getAddress()), ClientType.PROXY);
        }
    }

}
