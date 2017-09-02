package de.superioz.moo.network.packets;

import de.superioz.moo.network.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.network.packet.PacketBuffer;

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
     * The id of the server
     */
    public int id;

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
