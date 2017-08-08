package de.superioz.moo.cloud.database;

import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.cloud.Cloud;

/**
 * Wrapper class for every database collection
 */
public final class CloudCollections {

    public static final BanCollection BAN;
    public static final BanArchiveCollection BAN_ARCHIVE;
    public static final PlayerDataCollection PLAYER;
    public static final UniqueIdBufCollection UUID_BUFFER;
    public static final GroupCollection GROUP;

    static {
        BAN = Cloud.getInstance().getDatabaseCollection(DatabaseType.BAN);
        BAN_ARCHIVE = Cloud.getInstance().getDatabaseCollection(DatabaseType.BAN_ARCHIVE);
        PLAYER = Cloud.getInstance().getDatabaseCollection(DatabaseType.PLAYER);
        UUID_BUFFER = Cloud.getInstance().getDatabaseCollection(DatabaseType.UUID_BUFFER);
        GROUP = Cloud.getInstance().getDatabaseCollection(DatabaseType.GROUP);
    }

}
