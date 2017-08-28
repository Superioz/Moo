package de.superioz.moo.netty.packets;

import lombok.NoArgsConstructor;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packet.PacketBuffer;

import java.io.IOException;

/**
 * Dunno.
 */
@NoArgsConstructor
public class PacketKeepalive extends AbstractPacket {

    @Override
    public void read(PacketBuffer buf) throws IOException {
        // NOTHING
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        // NOTHING
    }

}
