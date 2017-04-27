package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.common.Response;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.events.QueryEvent;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packets.PacketRespond;

public class QueryServerListener implements EventListener {

    @EventHandler
    public void onQuery(QueryEvent event) {
        AbstractPacket packet = event.getToQueryPacket();

        packet.interceptRespond(abstractPacket -> {
            if(!(abstractPacket instanceof PacketRespond)){
                event.accept(new Response(ResponseStatus.NOK));
                return;
            }
            Response response = new Response((PacketRespond)abstractPacket);
            event.accept(response);
        });
        Cloud.getInstance().getServer().getNetworkBus().processIn(null, packet);
    }

}
