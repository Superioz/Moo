package de.superioz.moo.protocol.packets;

import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.UUID;

/**
 * Packet when a daemon sub-server has finished starting or stopping
 *
 * @see Type
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketServerDone extends AbstractPacket {

    public Type doneType;
    public UUID uuid;
    public String type;
    public int port;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.doneType = buf.readEnumValue(Type.class);
        this.uuid = buf.readUuid();
        this.type = buf.readString();
        this.port = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(doneType);
        buf.writeUuid(uuid);
        buf.writeString(type);
        buf.writeInt(port);
    }

    public enum Type {

        START,
        SHUTDOWN

    }

}
