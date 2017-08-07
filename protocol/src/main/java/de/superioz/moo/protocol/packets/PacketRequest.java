package de.superioz.moo.protocol.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;

/**
 * This packet is to request specific values from smth or someone
 * OR just to trigger
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketRequest extends AbstractPacket {

    /**
     * The type of value
     */
    public Type type;

    /**
     * Meta data (maybe a name or smth like that)
     */
    public String meta;

    public PacketRequest(Type type) {
        this(type, "");
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readEnumValue(Type.class);
        this.meta = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeString(meta);
    }

    public enum Type {

        PING,
        UPDATE_PERM

    }

}
