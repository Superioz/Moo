package de.superioz.moo.api.cache;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.io.MooConfigType;
import lombok.Getter;
import org.redisson.api.RFuture;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.UUID;

/**
 * This class is for caching player data and similar<br>
 * That includes: {@link PlayerData}s, permissions and {@link Group}s
 * <br>
 * It uses Redis for caching the data, it's a lot easier than normal maps
 *
 * @see org.redisson.Redisson
 */
@Getter
public final class MooCache {

    private static MooCache instance;
    private boolean initialized = false;

    public static synchronized MooCache getInstance() {
        if(instance == null) {
            instance = new MooCache();
        }
        return instance;
    }

    /**
     * This map stores the group behind the group's name
     */
    private RMap<String, Group> groupMap;

    /**
     * This map stores the playerData behind the player's uuid
     */
    private RMap<UUID, PlayerData> uniqueIdPlayerMap;

    /**
     * This map stores the player's uuid behind the player's name
     */
    private RMap<String, UUID> nameUniqueIdMap;

    /**
     * This map stores the permissions of one player
     */
    private RMap<UUID, List<String>> playerPermissionMap;

    /**
     * This map stores config values of the cloud
     */
    private RMap<String, Object> configMap;

    /**
     * This map stores all started servers behind their unique id
     */
    private RMap<UUID, MooServer> serverMap;

    /**
     * The client of the redis connection
     */
    private RedissonClient redisClient;

    /**
     * Initializes the {@link RMap}s of this cache.
     */
    public void initialize(RedisConnection connection) {
        this.redisClient = connection.getClient();

        // get redis maps by fetching the keys out of the config
        this.groupMap = redisClient.getMap(RedisConfig.GROUP_MAP.getKey());
        this.uniqueIdPlayerMap = redisClient.getMap(RedisConfig.PLAYER_DATA_MAP.getKey());
        this.nameUniqueIdMap = redisClient.getMap(RedisConfig.PLAYER_ID_MAP.getKey());
        this.playerPermissionMap = redisClient.getMap(RedisConfig.PLAYER_PERMISSION_MAP.getKey());
        this.configMap = redisClient.getMap(RedisConfig.CONFIG_MAP.getKey());
        this.serverMap = redisClient.getMap(RedisConfig.SERVER_MAP.getKey());

        this.initialized = true;
    }

    /**
     * Deletes all maps
     */
    public void delete(){
        if(!initialized) return;
        groupMap.deleteAsync();
        uniqueIdPlayerMap.deleteAsync();
        nameUniqueIdMap.deleteAsync();
        configMap.deleteAsync();
        serverMap.deleteAsync();

        initialized = false;
    }

    /**
     * Gets something from the config map
     *
     * @param type The type
     * @return The object
     */
    public Object getConfigEntry(MooConfigType type) {
        return configMap.get(type.getKey());
    }

    public RFuture<Object> getConfigEntryAsync(MooConfigType type) {
        return configMap.getAsync(type.name().toLowerCase());
    }

    /*
    ============================================
    SPECIAL METHODS
    ============================================
     */

}
