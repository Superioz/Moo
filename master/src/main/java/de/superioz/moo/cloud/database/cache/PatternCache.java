package de.superioz.moo.cloud.database.cache;

import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.objects.ServerPattern;

public class PatternCache extends DatabaseCache<String, ServerPattern> {

    public PatternCache(Builder builder) {
        super(builder);
    }
}
