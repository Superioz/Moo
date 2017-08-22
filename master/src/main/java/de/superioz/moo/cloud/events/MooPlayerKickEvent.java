package de.superioz.moo.cloud.events;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.protocol.packets.PacketPlayerPunish;

public class MooPlayerKickEvent extends PacketPlayerPunish.Event  {

    public MooPlayerKickEvent(PacketPlayerPunish packet, PlayerData data) {
        super(packet, data);
    }
}
