package de.superioz.moo.netty.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packet.PacketBuffer;

import java.io.IOException;
import java.net.InetSocketAddress;

@AllArgsConstructor
@NoArgsConstructor
public class PacketServerUnregister extends AbstractPacket {

    /**
     * The address of the server to be unregistered
     */
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
