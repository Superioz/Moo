package de.superioz.moo.cloud.database;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.cloud.database.cache.PatternCache;

import java.util.concurrent.TimeUnit;

public class PatternCollection extends DatabaseCollection<String, ServerPattern> {

    public PatternCollection(DatabaseConnection connection) {
        super(connection);
        super.architecture(ServerPattern.class);
        super.cache(new PatternCache(new DatabaseCache.Builder().database(this).expireAfterAccess(10, TimeUnit.MINUTES)));
    }

    @Override
    public String getName() {
        return DatabaseType.CLOUD_PATTERNS.getName();
    }
}
