package de.superioz.moo.cloud.database;

import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.cloud.Cloud;

/**
 * Wrapper class for every database collection
 */
public class CloudCollections {

    public static DatabaseCollection get(DatabaseType type) {
        return Cloud.getInstance().getDatabaseCollection(type);
    }

    public static BanCollection bans() {
        return Cloud.getInstance().getDatabaseCollection(DatabaseType.BAN);
    }

    public static MuteCollection mutes() {
        return Cloud.getInstance().getDatabaseCollection(DatabaseType.MUTE);
    }

    public static BanArchiveCollection banArchive() {
        return Cloud.getInstance().getDatabaseCollection(DatabaseType.BAN_ARCHIVE);
    }

    public static PlayerDataCollection players() {
        return Cloud.getInstance().getDatabaseCollection(DatabaseType.PLAYER);
    }

    public static UniqueIdBufCollection uniqueIds() {
        return Cloud.getInstance().getDatabaseCollection(DatabaseType.UUID_BUFFER);
    }

    public static GroupCollection groups() {
        return Cloud.getInstance().getDatabaseCollection(DatabaseType.GROUP);
    }

}
