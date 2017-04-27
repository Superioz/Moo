package de.superioz.moo.cloud.cache;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.object.Ban;

import java.util.UUID;

public class MuteCache extends DatabaseCache<UUID, Ban> {

    public MuteCache(DatabaseCache.Builder builder) {
        super(builder);
    }

}
