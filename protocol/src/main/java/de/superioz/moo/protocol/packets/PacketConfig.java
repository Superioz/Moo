package de.superioz.moo.protocol.packets;

import de.superioz.moo.api.io.MooConfigType;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * This packet is for sending config information across the network<br>
 * This is used to either change the cloud's config or to get information from
 * the cloud's config or to inform other instances about changes.
 *
 * @see MooConfigType
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketConfig extends AbstractPacket {

    /**
     * Type of the config
     */
    public MooConfigType type;

    /**
     * Meta informations (can be the new entry data)
     */
    public String meta;

    public PacketConfig(MooConfigType type) {
        this(type, "");
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readEnumValue(MooConfigType.class);
        this.meta = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeString(meta);
    }

}
