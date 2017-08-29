package de.superioz.moo.netty.packets;

import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * This packet is for sending config information across the network<br>
 * This is used to either change the cloud's config or to list information from
 * the cloud's config or to inform other instances about changes.
 *
 * @see NetworkConfigType
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketConfig extends AbstractPacket {

    /**
     * Type of the config
     */
    public NetworkConfigType type;

    /**
     * Meta informations (can be the new entry data)
     */
    public String meta;

    public PacketConfig(NetworkConfigType type) {
        this(type, "");
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readEnumValue(NetworkConfigType.class);
        this.meta = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeString(meta);
    }

}
