package de.superioz.moo.netty.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packet.PacketBuffer;

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
