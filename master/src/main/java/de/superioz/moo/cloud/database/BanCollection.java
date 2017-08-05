package de.superioz.moo.cloud.database;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.object.Ban;
import de.superioz.moo.cloud.database.cache.BanCache;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BanCollection extends DatabaseCollection<UUID, Ban> {

    public BanCollection(DatabaseConnection backend) {
        super(backend);
        super.architecture(Ban.class);
        super.cache(new BanCache(new DatabaseCache.Builder().database(this).expireAfterAccess(1, TimeUnit.HOURS)));
    }

    @Override
    public String getName() {
        return DatabaseType.BAN.getName();
    }

}
