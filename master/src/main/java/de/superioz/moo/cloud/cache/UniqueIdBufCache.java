package de.superioz.moo.cloud.cache;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.object.UniqueIdBuf;

public class UniqueIdBufCache extends DatabaseCache<String, UniqueIdBuf> {

    public UniqueIdBufCache(Builder builder) {
        super(builder);
    }
}
