package de.superioz.moo.cloud.events;

import de.superioz.moo.protocol.packets.PacketPlayerState;

/**
 * Event when the player joined a server
 */
public class MooPlayerJoinedServerEvent extends PacketPlayerState.Event {

    public MooPlayerJoinedServerEvent(PacketPlayerState packet) {
        super(packet);
    }

}
