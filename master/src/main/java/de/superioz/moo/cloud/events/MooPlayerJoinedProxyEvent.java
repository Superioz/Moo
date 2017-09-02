package de.superioz.moo.cloud.events;

import de.superioz.moo.network.packets.PacketPlayerState;

/**
 * Event when the player joins a proxy
 */
public class MooPlayerJoinedProxyEvent extends PacketPlayerState.Event {

    public MooPlayerJoinedProxyEvent(PacketPlayerState packet) {
        super(packet);
    }

}
