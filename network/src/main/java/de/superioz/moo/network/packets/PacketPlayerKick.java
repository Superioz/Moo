package de.superioz.moo.network.packets;

import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.UUID;

/**
 * This packet is for forcing the cloud to kick a player
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketPlayerKick extends AbstractPacket {

    public UUID executor;
    public UUID target;
    public String message;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.executor = buf.readUuid();
        this.target = buf.readUuid();
        this.message = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeUuid(executor);
        buf.writeUuid(target);
        buf.writeString(message);
    }
}
