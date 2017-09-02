package de.superioz.moo.network.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;

import java.io.IOException;

/**
 * Packet to request a daemon sub-server to be stopped
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketServerRequestShutdown extends AbstractPacket {

    public String host;
    public int port;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.host = buf.readString();
        this.port = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(host);
        buf.writeInt(port);
    }
}
