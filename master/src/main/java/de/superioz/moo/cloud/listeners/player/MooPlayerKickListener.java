package de.superioz.moo.cloud.listeners.player;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.event.EventPriority;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.events.MooPlayerKickEvent;
import de.superioz.moo.netty.common.ResponseStatus;
import de.superioz.moo.netty.packets.PacketPlayerBan;

/**
 * This class handles the player kick
 */
public class MooPlayerKickListener implements EventListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(MooPlayerKickEvent event) {
        PacketPlayerBan packet = event.getPacket();

        // gets the player from given id
        // if the player cannot be found, than just return a bad status
        PlayerData player = Cloud.getInstance().getMooProxy().getPlayer(event.getData().getUuid());
        if(player == null) {
            packet.respond(ResponseStatus.NOT_FOUND);
            return;
        }

        Cloud.getInstance().getMooProxy().kick(player, packet.deepCopy(), response -> packet.respond(response.getStatus()));
    }

}
