package de.superioz.moo.network.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.network.packet.PacketBuffer;

import java.io.IOException;

/**
 * Native version of {@link PacketDatabaseInfo}, that means without database type as enum but as string<br>
 * Will be without a cache
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketDatabaseInfoNative extends AbstractPacket {

    /**
     * The name of the database
     */
    public String databaseName;

    /**
     * The filter to fetch data (null should fetch all entries)
     */
    public DbFilter filter;

    /**
     * The limit of to-fetch data
     */
    public int limit = 0;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.databaseName = buf.readString();
        this.filter = new DbFilter().readObject(buf.readString());
        this.limit = buf.readInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(databaseName);
        buf.writeString(filter.toString());
        buf.writeInt(limit);
    }
}
