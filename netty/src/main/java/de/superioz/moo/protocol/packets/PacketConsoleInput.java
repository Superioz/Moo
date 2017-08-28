package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * This packet is for forcing a server to execute a commandline
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketConsoleInput extends AbstractPacket {

    public String commandline;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.commandline = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(commandline);
    }
}
