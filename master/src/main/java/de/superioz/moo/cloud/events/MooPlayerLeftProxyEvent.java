package de.superioz.moo.cloud.events;

import de.superioz.moo.network.packets.PacketPlayerState;

/**
 * Event when the player leaves the proxy
 */
public class MooPlayerLeftProxyEvent extends PacketPlayerState.Event {

    public MooPlayerLeftProxyEvent(PacketPlayerState packet) {
        super(packet);
    }

}
