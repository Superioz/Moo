package de.superioz.moo.api.database;

import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.objects.UniqueIdBuf;
import lombok.Getter;
import de.superioz.moo.api.database.objects.Ban;

/**
 * Implemented database types which are supported by the api<br>
 * These types can have their own {@link DatabaseCollection}
 */
public enum DatabaseType {

    PLAYER(PlayerData.class, "players"),
    GROUP(Group.class, "groups"),
    BAN(Ban.class, "bans"),
    BAN_ARCHIVE(Ban.class, "ban-archive"),
    UUID_BUFFER(UniqueIdBuf.class, "uuid-buffer");

    /**
     * The class which represents
     */
    @Getter
    private Class<?> wrappedClass;

    /**
     * The name of the database collection
     */
    @Getter
    private String name;

    DatabaseType(Class<?> c, String name) {
        this.wrappedClass = c;
        this.name = name;
    }

}
