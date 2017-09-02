package de.superioz.moo.cloud.database;

import de.superioz.moo.api.database.DatabaseCache;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.DbModifier;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.query.DbQuery;
import de.superioz.moo.cloud.database.cache.PlayerDataCache;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerDataCollection extends DatabaseCollection<UUID, PlayerData> {

    public PlayerDataCollection(DatabaseConnection backend) {
        super(backend);
        super.architecture(PlayerData.class);
        super.cache(new PlayerDataCache(new DatabaseCache.Builder().database(this).expireAfterAccess(1, TimeUnit.HOURS)));
    }

    @Override
    public String getName() {
        return DatabaseType.PLAYER.getName();
    }

    /**
     * Get the currentData, and set default values if not exists<br>
     * If update, then update name/ip/.. if different from before
     *
     * @param id The data to identify
     * @return The playerData
     */
    public PlayerData getCurrentData(PlayerData id, boolean update) {
        if(id == null) return null;
        UUID uuid = id.getUuid();
        String name = id.getLastName();

        // fail
        if(uuid == null) return null;

        PlayerData currentData = get(uuid);
        if(currentData == null) {
            // if the data not exists, we are gonna create the default profile for the player
            Group def = DatabaseCollections.GROUP.getDefault();
            id.setGroup(def.getName());
            id.setRank(def.getRank());
            id.setLastOnline(0L);
            id.setJoined(System.currentTimeMillis());
            id.setFirstOnline(System.currentTimeMillis());
            id.setTotalOnline(0L);
            id.setCoins(0L);
            id.setBanPoints(0);

            set(uuid, id, true);
            currentData = id;
        }
        else if(update) {
            DbQuery updates = new DbQuery(currentData.getClass());

            if(!currentData.getLastName().equals(id.getLastName())) {
                updates.equate(DbModifier.PLAYER_NAME, name);
            }
            if(!currentData.getLastIp().equals(id.getLastIp())) {
                updates.equate(DbModifier.PLAYER_IP, id.getLastIp());
            }
            if(!DatabaseCollections.GROUP.has(currentData.getGroup())) {
                Group def = DatabaseCollections.GROUP.getDefault();
                updates.equate(DbModifier.PLAYER_GROUP, def.getName()).equate(DbModifier.PLAYER_RANK, def.getRank());
                currentData.setGroup(def.getName());
            }

            set(uuid, currentData, updates.toMongoQuery().build(), true);
        }
        return currentData;
    }

}
