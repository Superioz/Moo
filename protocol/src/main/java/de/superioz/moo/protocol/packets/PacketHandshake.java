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

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readEnumValue(ClientType.class);
        this.identifier = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeString(identifier);
    }

}
