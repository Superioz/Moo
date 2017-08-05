package de.superioz.moo.cloud.database.cache;


import de.superioz.moo.api.cache.DatabaseCache;
import de.superioz.moo.api.database.object.PlayerData;

import java.util.UUID;

public class PlayerDataCache extends DatabaseCache<UUID, PlayerData> {

    public PlayerDataCache(Builder builder) {
        super(builder);
    }

}
