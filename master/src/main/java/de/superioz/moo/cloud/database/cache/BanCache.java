package de.superioz.moo.cloud.database.cache;

import de.superioz.moo.api.database.DatabaseCache;
import de.superioz.moo.api.database.objects.Ban;

import java.util.UUID;

public class BanCache extends DatabaseCache<UUID, Ban> {

    public BanCache(Builder builder) {
        super(builder);
    }

}
