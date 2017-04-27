package de.superioz.moo.cloud.database;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.*;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.database.object.UniqueIdBuf;
import de.superioz.moo.cloud.cache.UniqueIdBufCache;
import de.superioz.moo.protocol.common.Queries;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UniqueIdBufCollection extends DatabaseCollection<String, UniqueIdBuf> {

    public UniqueIdBufCollection(DatabaseConnection backend) {
        super(backend);
        super.architecture(UniqueIdBuf.class);
        super.cache(new UniqueIdBufCache(new DatabaseCache.Builder().database(this).expireAfterAccess(1, TimeUnit.HOURS)));
    }

    @Override
    public String getName() {
        return DatabaseType.UUID_BUFFER.getName();
    }

    /**
     * Gets the current uniqueIdBuf
     *
     * @param id The data to identify
     * @return The uniqueIdBuf
     */
    public UniqueIdBuf getCurrentBuf(PlayerData id, boolean update) {
        UUID uuid = id.uuid;
        String name = id.lastName;

        // fail
        if(uuid == null || name.isEmpty()) return null;

        UniqueIdBuf buf = get(name);
        if(buf == null) {
            buf = new UniqueIdBuf();
            buf.name = name;
            buf.uuid = uuid;

            set(name, buf, true);
        }
        else if(update) {
            Queries queries = Queries.newInstance(DatabaseType.UUID_BUFFER).filter(name);
            if(!buf.name.equals(id.lastName)){
                queries.equate(DbModifier.UUID_BUF_NAME, name);
            }
            queries.execute();

            DbQuery updates = new DbQuery(buf.getClass());

            if(!buf.name.equals(id.lastName)) {
                updates.add(DbModifier.UUID_BUF_NAME, DbQueryNode.Type.EQUATE, name);
            }

            set(name, buf, updates.toMongoQuery().build(), true);
        }
        return buf;
    }

}
