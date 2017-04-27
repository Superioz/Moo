package de.superioz.moo.cloud.events;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import de.superioz.moo.api.event.Event;
import de.superioz.moo.protocol.packets.PacketHandshake;

/**
 * This event is called when one clients handshakes with the cloud (authentication)
 */
@AllArgsConstructor
@Getter
public class HandshakeEvent implements Event {

    private Channel channel;
    private PacketHandshake packet;

}
