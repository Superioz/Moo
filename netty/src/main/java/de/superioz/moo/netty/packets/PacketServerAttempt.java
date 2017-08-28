package de.superioz.moo.netty.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.netty.packet.AbstractPacket;
import de.superioz.moo.netty.packet.PacketBuffer;

import java.io.IOException;
import java.util.UUID;

/**
 * Packet when a daemon sub-server attempts to start or stop
 *
 * @see Type
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketServerAttempt extends AbstractPacket {

    public Type type;
    public UUID id;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readEnumValue(Type.class);
        this.id = buf.readUuid();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeUuid(id);
    }

    public enum Type {

        START,
        SHUTDOWN

    }

}
