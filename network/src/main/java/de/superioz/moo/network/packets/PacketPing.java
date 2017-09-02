package de.superioz.moo.network.packets;

import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * U know what ping means, don't ya?
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketPing extends AbstractPacket {

    /**
     * The timestamp of the first instance
     */
    public long timestamp;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.timestamp = buf.readLong();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeLong(timestamp);
    }

}
