package de.superioz.moo.network.packets;

import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.net.InetSocketAddress;

@NoArgsConstructor
@AllArgsConstructor
public class PacketServerHeartBeat extends AbstractPacket {

    /**
     * The address of the server
     */
    public InetSocketAddress serverAddress;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.serverAddress = new InetSocketAddress(buf.readString(), buf.readInt());
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(serverAddress.getHostName());
        buf.writeInt(serverAddress.getPort());
    }
}
