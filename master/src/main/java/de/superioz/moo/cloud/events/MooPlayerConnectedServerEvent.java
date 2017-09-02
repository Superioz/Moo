package de.superioz.moo.cloud.events;

import de.superioz.moo.network.packets.PacketPlayerState;

/**
 * Event when the player connects to a server
 */
public class MooPlayerConnectedServerEvent extends PacketPlayerState.Event {

    public MooPlayerConnectedServerEvent(PacketPlayerState packet) {
        super(packet);
    }

}
