package de.superioz.moo.cloud.events;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.netty.events.PunishmentEvent;
import de.superioz.moo.netty.packets.PacketPlayerBan;

public class MooPlayerKickEvent extends PunishmentEvent {

    public MooPlayerKickEvent(PacketPlayerBan packet, PlayerData data) {
        super(packet, data);
    }
}
