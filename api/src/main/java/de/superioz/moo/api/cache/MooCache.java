package de.superioz.moo.api.cache;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.io.JsonConfig;
import lombok.Getter;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Map;
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
    private Map<UUID, List<String>> playerPermissionMap;

    /**
     * This map stores config values of the cloud
     */
    private RMap<String, String> configMap;

    /**
     * This map stores all started servers behind their unique id
     */
    private RMap<UUID, MooServer> serverMap;

    /**
     * The client of the redis connection
     */
    private RedissonClient redisClient;

    /**
     * Initializes the {@link RMap}s of this cache but only if the {@link RedissonClient} is gucci<br>
     *
     * @param config The config file of the instance to get all map's keys
     */
    public void initialize(RedissonClient client, JsonConfig config) {
        this.redisClient = client;
        if(client != null && !client.isShutdown()) return;

        // get redis maps by fetching the keys out of the config
        this.groupMap = client.getMap(config.get(RedisConfig.GROUP_MAP.getKey()));
        this.uniqueIdPlayerMap = client.getMap(config.get(RedisConfig.PLAYER_DATA_MAP.getKey()));
        this.nameUniqueIdMap = client.getMap(config.get(RedisConfig.PLAYER_ID_MAP.getKey()));
        this.playerPermissionMap = client.getMap(config.get(RedisConfig.PLAYER_PERMISSION_MAP.getKey()));
        this.configMap = client.getMap(config.get(RedisConfig.CONFIG_MAP.getKey()));
        this.serverMap = client.getMap(config.get(RedisConfig.SERVER_MAP.getKey()));
    }

}
