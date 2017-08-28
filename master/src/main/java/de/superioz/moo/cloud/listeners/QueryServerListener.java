package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.common.Response;
import de.superioz.moo.netty.common.ResponseStatus;
import de.superioz.moo.netty.events.QueryEvent;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packets.PacketRespond;

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
