package de.superioz.moo.protocol.packets;

import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
public class PacketUpdatePermission extends AbstractPacket {

    public DatabaseType type;
    public String key;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.type = buf.readEnumValue(DatabaseType.class);
        this.key = buf.readString();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(type);
        buf.writeString(key);
    }
}
