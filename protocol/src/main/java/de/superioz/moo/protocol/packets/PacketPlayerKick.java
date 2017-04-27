package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;

/**
 * This packet is for forcing the cloud to kick a player
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerKick extends AbstractPacket {

    public String from;
    public String id;
    public String message;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.from = buf.readString();
        this.id = buf.readString();
        this.message = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(from);
        buf.writeString(id);
        buf.writeString(message);
    }
}
