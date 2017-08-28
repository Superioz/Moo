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
     * The mode of the counting
     */
    public CountType countType;

    /**
     * Limit of the operation
     */
    public int limit;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.databaseType = buf.readEnumValue(DatabaseType.class);
        this.countType = buf.readEnumValue(CountType.class);
        this.limit = buf.readVarInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(databaseType);
        buf.writeEnumValue(countType);
        buf.writeVarInt(limit);
    }

    public enum CountType {

        NUMBER,
        LIST

    }

}
