package de.superioz.moo.protocol.packets;

import de.superioz.moo.api.database.DatabaseCollection;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packet.PacketBuffer;

import java.io.IOException;

/**
 * This packet is for counting or listing entries from a {@link DatabaseCollection}
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketDatabaseCount extends AbstractPacket {

    /**
     * The database type as enum
     */
    public DatabaseType databaseType;

    /**
     * The mode of the counting (0 = as number; >0 = as list)
     */
    public int mode;

    /**
     * Limit of the operation
     */
    public int limit;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.databaseType = buf.readEnumValue(DatabaseType.class);
        this.mode = buf.readVarInt();
        this.limit = buf.readVarInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(databaseType);
        buf.writeVarInt(mode);
        buf.writeVarInt(limit);
    }
}
