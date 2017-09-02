package de.superioz.moo.cloud.database.cache;


import de.superioz.moo.api.database.DatabaseCache;
import de.superioz.moo.api.database.objects.PlayerData;

import java.util.UUID;

public class PlayerDataCache extends DatabaseCache<UUID, PlayerData> {

    public PlayerDataCache(Builder builder) {
        super(builder);
    }

}
