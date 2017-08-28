package de.superioz.moo.netty.packets;

import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.netty.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.netty.packet.PacketBuffer;

import java.io.IOException;

/**
 * This packet is for fetching data from a {@link DatabaseCollection}
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketDatabaseInfo extends AbstractPacket {

    /**
     * The database type as enum
     */
    public DatabaseType databaseType;

    /**
     * The filter to fetch data (null should fetch all entries)
     */
    public DbFilter filter;

    /**
     * If true the entries from the cache will be streamed instead of looped
     */
    public boolean queried;

    /**
     * Limit of entries to-fetch (-1 for infinite)
     */
    public int limit;

    public PacketDatabaseInfo(DatabaseType type, DbFilter filter) {
        this(type, filter, false, -1);
    }

    public PacketDatabaseInfo(DatabaseType type, Class<?> c, Object primVal) {
        this(type, DbFilter.fromPrimKey(c, primVal));
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.databaseType = buf.readEnumValue(DatabaseType.class);
        this.filter = new DbFilter().readObject(buf.readString());
        this.queried = buf.readBoolean();
        this.limit = buf.readVarInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(databaseType);
        buf.writeString(filter.toString());
        buf.writeBoolean(queried);
        buf.writeVarInt(limit);
    }

}
