package de.superioz.moo.protocol.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;

@AllArgsConstructor
@NoArgsConstructor
public class PacketServerUnregister extends AbstractPacket {

    public InetSocketAddress address;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.address = new InetSocketAddress(buf.readString(), buf.readInt());
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(address.getHostName());
        buf.writeInt(address.getPort());
    }
}
