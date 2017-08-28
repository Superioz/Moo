package de.superioz.moo.protocol.packets;

import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;

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
