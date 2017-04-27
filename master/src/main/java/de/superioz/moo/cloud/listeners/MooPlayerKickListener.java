package de.superioz.moo.cloud.listeners;

import de.superioz.moo.api.common.MooPlayer;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.events.MooPlayerKickEvent;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

public class MooPlayerKickListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerKickEvent event) {
        PacketPlayerPunish packet = event.getPacket();

        // gets the player from given id
        // if the player cannot be found, than just return a bad status
        MooPlayer player = Cloud.getInstance().getMooProxy().getPlayer(event.getData().uuid);
        if(player == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        Cloud.getInstance().getMooProxy().kick(player, packet.deepCopy(), response -> packet.respond(response.getStatus()));
    }

}
