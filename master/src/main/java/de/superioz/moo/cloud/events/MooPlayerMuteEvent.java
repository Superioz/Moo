package de.superioz.moo.cloud.events;

import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

public class MooPlayerMuteEvent extends PacketPlayerPunish.Event  {

    public MooPlayerMuteEvent(PacketPlayerPunish packet, PlayerData data) {
        super(packet, data);
    }
}
