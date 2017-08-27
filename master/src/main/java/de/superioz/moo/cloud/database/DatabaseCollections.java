package de.superioz.moo.cloud.database;

import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.cloud.Cloud;

/**
 * Wrapper class for every database collection defined as {@link de.superioz.moo.api.database.DatabaseCollection}
 */
public final class DatabaseCollections {

    public static final BanCollection BAN;
    public static final BanArchiveCollection BAN_ARCHIVE;
    public static final PlayerDataCollection PLAYER;
    public static final GroupCollection GROUP;
    public static final PatternCollection PATTERN;

    static {
        BAN = Cloud.getInstance().getDatabaseCollection(DatabaseType.BAN);
        BAN_ARCHIVE = Cloud.getInstance().getDatabaseCollection(DatabaseType.BAN_ARCHIVE);
        PLAYER = Cloud.getInstance().getDatabaseCollection(DatabaseType.PLAYER);
        GROUP = Cloud.getInstance().getDatabaseCollection(DatabaseType.GROUP);
        PATTERN = Cloud.getInstance().getDatabaseCollection(DatabaseType.CLOUD_PATTERNS);
    }

}
