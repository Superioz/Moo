package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.api.reaction.Reaction;
import de.superioz.moo.api.reaction.Reactor;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.MooClientConnectedEvent;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.MultiPacket;
import de.superioz.moo.protocol.packets.PacketServerRegister;
import de.superioz.moo.protocol.server.MooClient;

import java.util.ArrayList;
import java.util.List;

public class MooClientConnectedListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClientConnected(MooClientConnectedEvent event) {
        MooClient client = event.getClient();

        // oh a proxy tries to connect to the server
        Reaction.react(client.getType(), new Reactor<ClientType>(ClientType.PROXY) {
            @Override
            public void invoke() {
                // packet register server
                List<PacketServerRegister> list = new ArrayList<>();

                for(MooServer daemon : Cloud.getInstance().getMooProxy().getSpigotServer().values()) {
                    list.add(new PacketServerRegister(daemon.getType(), daemon.getAddress().getHostName(), daemon.getAddress().getPort()));
                }
                PacketMessenger.message(new MultiPacket<>(list), client);
            }
        });
    }

}
