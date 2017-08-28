package de.superioz.moo.cloud.events;

import de.superioz.moo.netty.events.PunishmentEvent;
import lombok.Getter;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.netty.packets.PacketPlayerBan;

/**
 * Event after the player got banned
 */
public class MooPlayerPostBanEvent extends PunishmentEvent {

    @Getter
    private Ban ban;

    public MooPlayerPostBanEvent(Ban ban, PacketPlayerBan packet, PlayerData data) {
        super(packet, data);
        this.ban = ban;
    }

}
