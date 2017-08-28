package de.superioz.moo.netty.packets;

import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * This packet is for sending the console output to another server, which means
 * only a plain text message, but ... it's n-not that it's not i-important ... baka!
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketConsoleOutput extends AbstractPacket {

    public String message;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.message = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(message);
    }

}
