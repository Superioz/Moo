package de.superioz.moo.protocol.packets;

import de.superioz.moo.api.common.PlayerInfo;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * This packet is for receiving all informations about one player.<br>
 *
 * @see PlayerInfo
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerInfo extends AbstractPacket {

    public String id;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.id = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(id);
    }
}
