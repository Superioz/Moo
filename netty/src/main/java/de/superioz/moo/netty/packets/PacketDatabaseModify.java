package de.superioz.moo.netty.packets;

import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.netty.packet.AbstractPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.filter.DbFilter;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.database.DatabaseModifyType;
import de.superioz.moo.netty.packet.PacketBuffer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This packet is for modifying data from a {@link DatabaseCollection}
 */
@NoArgsConstructor
@AllArgsConstructor
public class PacketDatabaseModify extends AbstractPacket {

    /**
     * The database type as enum
     */
    public DatabaseType databaseType;

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
     * If true the entries from the cache will be streamed instead of looped
     */
    public boolean queried;

    /**
     * Limit of entries to-fetch (-1 for infinite)
     */
    public int limit;

    public PacketDatabaseModify(DatabaseType databaseType, DbFilter filter, DatabaseModifyType type, DbQuery updates){
        this(databaseType, filter, type, updates, false, -1);
    }

    /**
     * DatabaseModify packet to create an object
     *
     * @param module  The modules
     * @param object  The object
     * @param queried Queried
     * @param limit   Limit
     * @return Packet
     */
    public static <T> PacketDatabaseModify onCreate(DatabaseType module, T object, boolean queried, int limit) {
        DbFilter filter = DbFilter.fromObjectsPrimKey(object.getClass(), object);

        return new PacketDatabaseModify(module, filter, DatabaseModifyType.CREATE, DbQuery.fromObject(object), queried, limit);
    }

    public static <T> PacketDatabaseModify onCreate(DatabaseType module, T object) {
        return onCreate(module, object, false, -1);
    }

    /**
     * DatabaseModify packets to delete an object
     *
     * @param module  The modules
     * @param filter  The filter
     * @param queried Queried
     * @param limit   Limit
     * @return Packet
     */
    public static PacketDatabaseModify onDelete(DatabaseType module, DbFilter filter, boolean queried, int limit) {
        return new PacketDatabaseModify(module, filter, DatabaseModifyType.DELETE, null, queried, limit);
    }

    public static PacketDatabaseModify onDelete(DatabaseType module, DbFilter filter) {
        return onDelete(module, filter, false, -1);
    }

    /**
     * DatabaseModify packets to modify an object
     *
     * @param module  The modules
     * @param filter  The filter
     * @param queried Queried
     * @param limit   Limit
     * @return Packet
     */
    public static PacketDatabaseModify onModify(DatabaseType module, DbFilter filter, DbQuery updates, boolean queried, int limit){
        return new PacketDatabaseModify(module, filter, DatabaseModifyType.MODIFY, updates, queried, limit);
    }

    public static PacketDatabaseModify onModify(DatabaseType module, DbFilter filter, DbQuery updates) {
        return onModify(module, filter, updates, false, -1);
    }

    @Override
    public void read(PacketBuffer buf) throws IOException {
        this.databaseType = buf.readEnumValue(DatabaseType.class);
        this.filter = new DbFilter().readObject(buf.readString());
        this.type = buf.readEnumValue(DatabaseModifyType.class);

        this.updates = DbQuery.fromStringList(buf.readStringList());
        if(updates == null) buf.readString();
        else this.updates.setKeyHoldingClass(ReflectionUtil.getClass(buf.readString()));

        this.queried = buf.readBoolean();
        this.limit = buf.readVarInt();
    }

    @Override
    public void write(PacketBuffer buf) throws IOException {
        buf.writeEnumValue(databaseType);
        buf.writeString(filter.toString());
        buf.writeEnumValue(type);

        buf.writeStringList(updates == null ? new ArrayList<>() : updates.toStringList());
        buf.writeString(updates == null ? getClass().getSimpleName() : updates.getKeyHoldingClass().getName());

        buf.writeBoolean(queried);
        buf.writeVarInt(limit);
    }

}
