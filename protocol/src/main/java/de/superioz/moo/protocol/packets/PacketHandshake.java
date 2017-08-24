package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;

/**
 * This packet is for the handshake between two instances (at least a bit of authentication)
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketHandshake extends AbstractPacket {

    /**
     * The identifier of the instance (would be "skypvp" for example)
     */
    public String identifier;

    /**
     * The type of the instance to be connected
     */
    public ClientType type;

    /**
     * The subport of the server. If it stays -1 then it's not a spigot server.
     * If it is a spigot server this port is the port the server runs on
     */
    public int subPort = -1;

    public PacketHandshake(String identifier, ClientType type) {
        this.identifier = identifier;
        this.type = type;
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readEnumValue(ClientType.class);
        this.identifier = buf.readString();
        this.subPort = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeString(identifier);
        buf.writeInt(subPort);
    }

}
