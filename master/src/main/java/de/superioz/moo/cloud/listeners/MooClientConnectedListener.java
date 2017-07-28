package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.reaction.Reactor;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.MooClientConnectedEvent;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.MultiPacket;
import de.superioz.moo.protocol.packets.PacketServerRegister;
import de.superioz.moo.protocol.server.MooClient;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MooClientConnectedListener implements EventListener {

    private static final Pattern PREDEFINED_SERVER_PATTERN = Pattern.compile("\\w+(:\\d+)?");

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClientConnected(MooClientConnectedEvent event) {
        MooClient client = event.getClient();

        // oh a proxy tries to connect to the server
        Reaction.react(client.getType(), new Reactor<ClientType>(ClientType.PROXY) {
            @Override
            public void invoke() {
                // send already registered server to the proxy
                List<PacketServerRegister> list = new ArrayList<>();
                for(MooServer server : Cloud.getInstance().getMooProxy().getSpigotServers().values()) {
                    list.add(new PacketServerRegister(server.getType(), server.getAddress().getHostName(), server.getAddress().getPort()));
                }
                PacketMessenger.message(new MultiPacket<>(list), client);

                // start predefined servers
                // ONLY if the serverlist inside config is not empty, nor null
                // AND if a daemon is connected (which will be checked automatically)
                List<String> predefinedServers = Cloud.getInstance().getConfig().get("predefined-servers");
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
        });
    }

}
