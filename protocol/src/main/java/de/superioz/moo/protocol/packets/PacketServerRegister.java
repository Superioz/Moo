package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;

/**
 * Packet when a server has to be registered
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketServerRegister extends AbstractPacket {

    /**
     * The type of the server
     */
    public String type;

    /**
     * The host of the server
     */
    public String host;

    /**
     * The port of the server
     */
    public int port;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readString();
        this.host = buf.readString();
        this.port = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(type);
        buf.writeString(host);
        buf.writeInt(port);
    }
}
