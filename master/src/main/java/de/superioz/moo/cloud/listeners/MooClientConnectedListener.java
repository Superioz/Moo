package de.superioz.moo.cloud.listeners;

import de.superioz.moo.network.common.MooServer;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.network.client.ClientType;
import de.superioz.moo.network.common.PacketMessenger;
import de.superioz.moo.network.events.MooClientConnectedEvent;
import de.superioz.moo.network.packets.MultiPacket;
import de.superioz.moo.network.packets.PacketServerRegister;
import de.superioz.moo.network.client.MooClient;
import de.superioz.moo.network.common.MooProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * This class listens on a client connecting to the cloud
 */
public class MooClientConnectedListener implements EventListener {

    //private static final Pattern PREDEFINED_SERVER_PATTERN = Pattern.compile("\\w+(:\\d+)?");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClientConnected(MooClientConnectedEvent event) {
        MooClient client = event.getClient();
        if(Cloud.getInstance().getNetworkProxy() == null) {
            Cloud.getInstance().getLogger().severe(ConsoleColor.RED
                    + "Couldn't accept client because the MooProxy didn't initialize properly!");
            return;
        }

        // BUNGEE BUNGEE BUNGEE oh a proxy connects to the server
        if(client.getType() == ClientType.PROXY) {
            // send already registered server to the proxy
            List<PacketServerRegister> list = new ArrayList<>();
            for(MooServer server : Cloud.getInstance().getNetworkProxy().getSpigotServers().values()) {
                list.add(new PacketServerRegister(
                        server.getType(),
                        server.getAddress().getHostName(),
                        server.getId(),
                        server.getAddress().getPort())
                );
            }

            Cloud.getInstance().getLogger().debug("Send already registered server to proxy (" + list.size() + "x) ..");
            MultiPacket<PacketServerRegister> multiPacket = new MultiPacket<>(list);
            PacketMessenger.message(multiPacket, client);

            // WE START THE SERVERS ON PROXY CONNECT because that way it is possible to use multiple daemons
            // if this is not the first proxy, rip
            // OR if no daemon has been found
            if(Cloud.getInstance().getClientManager().getClients(ClientType.PROXY).size() > 1
                    || Cloud.getInstance().getClientManager().getClients(ClientType.DAEMON).size() == 0) {
                return;
            }

            // start minimum amount of servers
            MooProxy.serverCycleAll();
        }
        // SPIGOT SPIGOT SPIGOT A server connects to the server
        else if(client.getType() == ClientType.SERVER) {
            String ip = client.getAddress().getHostName() + ":" + client.getSubPort();
            Cloud.getInstance().getLogger().debug("Register server " + ip + " with type '" + client.getName() + "' ..");

            // register server
            MooServer server = Cloud.getInstance().getNetworkProxy().registerServer(client);

            // what do we do now? YEAH we inform the proxies
            PacketMessenger.message(new PacketServerRegister(client.getName(), client.getAddress().getHostName(), server.getId(), client.getSubPort()),
                    ClientType.PROXY);
        }
    }

}
