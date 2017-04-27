package de.superioz.moo.protocol.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketServerUnregister extends AbstractPacket {

    public int port;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.port = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeInt(port);
    }
}
