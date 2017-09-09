package de.superioz.moo.client.listeners;

import de.superioz.moo.client.Moo;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.network.common.PacketMessenger;
import de.superioz.moo.network.queries.Response;
import de.superioz.moo.network.events.QueryEvent;
import de.superioz.moo.network.exception.MooOutputException;
import de.superioz.moo.network.packet.AbstractPacket;

import java.util.function.Consumer;

public class QueryClientListener implements EventListener {

    @EventHandler
    public void onQuery(QueryEvent event) {
        if(!Moo.getInstance().isConnected()){
            event.setCancelled(true);
            event.setCancelReason(new MooOutputException(MooOutputException.Type.CONNECTION_FAILED));
            return;
        }
        AbstractPacket packet = event.getToQueryPacket();

        PacketMessenger.transferToResponse(packet, (Consumer<Response>) event::accept);
    }

}
