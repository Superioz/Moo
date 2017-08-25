package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.events.MooClientConnectedEvent;
import de.superioz.moo.protocol.packets.MultiPacket;
import de.superioz.moo.protocol.packets.PacketServerRegister;
import de.superioz.moo.protocol.server.MooClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class listens on a client connecting to the cloud
 */
public class MooClientConnectedListener implements EventListener {

    private static final Pattern PREDEFINED_SERVER_PATTERN = Pattern.compile("\\w+(:\\d+)?");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClientConnected(MooClientConnectedEvent event) {
        MooClient client = event.getClient();
        if(Cloud.getInstance().getMooProxy() == null){
            Cloud.getInstance().getLogger().severe(ConsoleColor.RED
                    + "Couldn't accept client because the MooProxy didn't initialize properly!");
            return;
        }

        // BUNGEE BUNGEE BUNGEE oh a proxy connects to the server
        if(client.getType() == ClientType.PROXY) {
            // send already registered server to the proxy
            List<PacketServerRegister> list = new ArrayList<>();
            for(MooServer server : Cloud.getInstance().getMooProxy().getSpigotServers().values()) {
                list.add(new PacketServerRegister(server.getType(), server.getAddress().getHostName(), server.getAddress().getPort()));
            }

            Cloud.getInstance().getLogger().debug("Send already registered server to proxy (" + list.size() + "x) ..");
            MultiPacket<PacketServerRegister> multiPacket = new MultiPacket<>(list);
            PacketMessenger.message(multiPacket, client);
        }
        // DAEMON DAEMON DAEMON A daemon connects to the server!
        else if(client.getType() == ClientType.DAEMON) {
            // if this is not the first daemon, rip
            if(Cloud.getInstance().getClientManager().getClients(ClientType.DAEMON).size() > 1) {
                return;
            }

            // start predefined servers
            // ONLY if the serverlist inside config is not empty, nor null
            // AND ONLY IF AUTOMATIC MODE IS ACTIVATED
            if(!(boolean) Cloud.getInstance().getConfig().get("predefined-servers.activated")) {
                return;
            }

            // otherwise start
            List<String> predefinedServers = Cloud.getInstance().getConfig().get("predefined-servers.list");
            if(predefinedServers != null && !predefinedServers.isEmpty()) {
                for(String server : predefinedServers) {
                    if(!PREDEFINED_SERVER_PATTERN.matcher(server).matches()) continue;
                    String[] split = server.split(":");
                    String type = split[0];
                    int amount = split.length > 1 && Validation.INTEGER.matches(split[1]) ? Integer.parseInt(split[1]) : 1;

                    Cloud.getInstance().getMooProxy().requestServer(type, false, amount, resultServer -> {
                    });
                }
            }
        }
        // SPIGOT SPIGOT SPIGOT A server connects to the server
        else if(client.getType() == ClientType.SERVER) {
            String ip = client.getAddress().getHostName() + ":" + client.getSubPort();
            Cloud.getInstance().getLogger().debug("Register server " + ip + " with type '" + client.getName() + "' ..");

            // register server
            Cloud.getInstance().getMooProxy().registerServer(client);

            // what do we do now? YEAH we inform the proxies
            PacketMessenger.message(new PacketServerRegister(client.getName(), client.getAddress().getHostName(), client.getSubPort()),
                    ClientType.PROXY);
        }
    }

}
