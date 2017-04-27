package de.superioz.moo.cloud.database;

import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.object.Ban;
import de.superioz.moo.cloud.cache.MuteCache;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MuteCollection extends DatabaseCollection<UUID, Ban> {

    public MuteCollection(DatabaseConnection backend) {
        super(backend);
        super.architecture(Ban.class);
        super.cache(new MuteCache(new MuteCache.Builder().database(this).expireAfterAccess(1, TimeUnit.HOURS)));
    }

    @Override
    public String getName() {
        return DatabaseType.MUTE.getName();
    }

}
