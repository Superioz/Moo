package de.superioz.moo.cloud.cache;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.object.Ban;

import java.util.UUID;

public class BanCache extends DatabaseCache<UUID, Ban> {

    public BanCache(Builder builder) {
        super(builder);
    }

}
