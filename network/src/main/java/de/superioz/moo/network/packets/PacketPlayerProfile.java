package de.superioz.moo.network.packets;

import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * This packet is for receiving all informations about one player.<br>
 *
 * @see PlayerProfile
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerProfile extends AbstractPacket {

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
