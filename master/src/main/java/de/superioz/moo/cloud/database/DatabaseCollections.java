package de.superioz.moo.cloud.database;

import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.modules.DatabaseModule;

/**
 * Wrapper class for every database collection defined as {@link de.superioz.moo.api.database.DatabaseCollection}
 */
public final class DatabaseCollections {

    public static BanCollection BAN;
    public static BanArchiveCollection BAN_ARCHIVE;
    public static PlayerDataCollection PLAYER;
    public static GroupCollection GROUP;
    public static PatternCollection PATTERN;

    /**
     * Initializes this class LOL
     *
     * @param module The module to get the .. from
     */
    public static void init(DatabaseModule module) {
        BAN = Cloud.getInstance().getDatabaseCollection(module, DatabaseType.BAN);
        BAN_ARCHIVE = Cloud.getInstance().getDatabaseCollection(module, DatabaseType.BAN_ARCHIVE);
        PLAYER = Cloud.getInstance().getDatabaseCollection(module, DatabaseType.PLAYER);
        GROUP = Cloud.getInstance().getDatabaseCollection(module, DatabaseType.GROUP);
        PATTERN = Cloud.getInstance().getDatabaseCollection(module, DatabaseType.CLOUD_PATTERNS);
    }

}
