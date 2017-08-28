package de.superioz.moo.protocol.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;

/**
 * Packet to inform cloud about the current ram usage of one daemon instance
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketRamUsage extends AbstractPacket {

    public int ramUsage;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.ramUsage = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeInt(ramUsage);
    }
}
