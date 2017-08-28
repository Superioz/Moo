package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.cloud.events.MooServerRestockEvent;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketServerRequest;
import de.superioz.moo.protocol.server.MooProxy;

public class MooServerRestockListener implements EventListener {

    @EventHandler
    public void onServerRestock(MooServerRestockEvent event) {
        // check pattern
        ServerPattern pattern = event.getPattern();
        if(pattern == null) {
            return;
        }

        // get amount of servers to start
        int current = MooProxy.getInstance().getServer(pattern.getName()).size();
        int diff = pattern.getMin() > current ? (current - pattern.getMin()) : 0;
        if(diff == 0) return;

        // send packet!
        PacketMessenger.message(new PacketServerRequest(pattern.getName(), false, diff));
    }

}
