package de.superioz.moo.network.packets;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.network.packet.AbstractPacket;
import de.superioz.moo.api.database.DatabaseModifyType;
import de.superioz.moo.network.packet.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The native version of {@link PacketDatabaseModify}, that means without database type as enum but as string.<br>
 * Will be without a cache and informing the servers
 */
@AllArgsConstructor
@NoArgsConstructor
public class PacketDatabaseModifyNative extends AbstractPacket {

    /**
     * The name of the database
     */
    public String databaseName;

    /**
     * The filter to fetch data (null should fetch all entries)
     */
    public DbFilter filter;

    /**
     * The modifying type (e.g. {@link DatabaseModifyType#CREATE})
     */
    public DatabaseModifyType type;

    /**
     * The updates of the modification or the data of the object to be created
     */
    public DbQuery updates;

    /**
     * The limit of to-fetch data
     */
    public int limit = 0;

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.databaseName = buf.readString();
        this.filter = new DbFilter().readObject(buf.readString());
        this.type = buf.readEnumValue(DatabaseModifyType.class);

        this.updates = DbQuery.fromStringList(buf.readStringList());
        if(updates == null) buf.readString();
        else this.updates.setKeyHoldingClass(ReflectionUtil.getClass(buf.readString()));

        this.limit = buf.readVarInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeString(databaseName);
        buf.writeString(filter.toString());
        buf.writeEnumValue(type);

        buf.writeStringList(updates == null ? new ArrayList<>() : updates.toStringList());
        buf.writeString(updates == null ? getClass().getSimpleName() : updates.getKeyHoldingClass().getName());
        buf.writeVarInt(limit);
    }
}
