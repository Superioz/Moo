package de.superioz.moo.cloud.events;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.netty.events.PunishmentEvent;
import de.superioz.moo.netty.packets.PacketPlayerBan;

/**
 * Event when the player gets banned
 */
public class MooPlayerBanEvent extends PunishmentEvent {

    public MooPlayerBanEvent(PacketPlayerBan packet, PlayerData data) {
        super(packet, data);
    }

}
