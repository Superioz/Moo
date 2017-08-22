package de.superioz.moo.cloud.events;

import lombok.Getter;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

/**
 * Event after the player got banned
 */
public class MooPlayerPostBanEvent extends PacketPlayerPunish.Event {

    @Getter
    private Ban ban;

    public MooPlayerPostBanEvent(Ban ban, PacketPlayerPunish packet, PlayerData data) {
        super(packet, data);
        this.ban = ban;
    }

}
