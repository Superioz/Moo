package de.superioz.moo.cloud.events;

import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

/**
 * Event when the player gets banned
 */
public class MooPlayerBanEvent extends PacketPlayerPunish.Event {

    public MooPlayerBanEvent(PacketPlayerPunish packet, PlayerData data) {
        super(packet, data);
    }

}
