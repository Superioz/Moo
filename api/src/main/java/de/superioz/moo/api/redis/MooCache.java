package de.superioz.moo.api.redis;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.objects.ServerPattern;
import lombok.Getter;
import org.redisson.api.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

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

    // we're just gonna use 3 seconds as local cache,
    // because we don't want to go cacheless and 3s cached is better than 0s D:
    // AND this is just for the default redis caches, so do not worry
    private static final LocalCachedMapOptions DEFAULT_OPTIONS = LocalCachedMapOptions.defaults()
            .timeToLive(30, TimeUnit.SECONDS).maxIdle(30, TimeUnit.SECONDS);
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
    private RLocalCachedMap<String, Group> groupMap;

    /**
     * This map stores the playerData behind the player's uuid
     */
    private RLocalCachedMap<UUID, PlayerData> playerMap;

    /**
     * This map stores the player's uuid behind the player's name
     */
    private RLocalCachedMap<String, UUID> nameUniqueIdMap;

    /**
     * This map stores the permissions of one player
     */
    private RLocalCachedMap<UUID, List<String>> playerPermissionMap;

    /**
     * This map stores config values of the cloud
     */
    private RLocalCachedMap<String, Object> configMap;

    /**
     * This map stores all started servers behind their unique id
     */
    private RLocalCachedMap<UUID, MooServer> serverMap;

    /**
     * This map stores the serverPattern behind their name as key
     */
    private RLocalCachedMap<String, ServerPattern> patternMap;

    /**
     * The client of the redis connection
     */
    private RedissonClient redisClient;

    /**
     * Initializes the {@link RMap}s of this cache.
     */
    public void initialize(RedisConnection connection) {
        this.redisClient = connection.getClient();

        // list redis maps by fetching the keys out of the config
        this.groupMap = redisClient.getLocalCachedMap(RedisConfig.GROUP_MAP.getKey(), DEFAULT_OPTIONS);
        this.playerMap = redisClient.getLocalCachedMap(RedisConfig.PLAYER_MAP.getKey(), DEFAULT_OPTIONS);
        this.nameUniqueIdMap = redisClient.getLocalCachedMap(RedisConfig.PLAYER_ID_MAP.getKey(), DEFAULT_OPTIONS);
        this.playerPermissionMap = redisClient.getLocalCachedMap(RedisConfig.PLAYER_PERMISSION_MAP.getKey(), DEFAULT_OPTIONS);
        this.configMap = redisClient.getLocalCachedMap(RedisConfig.CONFIG_MAP.getKey(), DEFAULT_OPTIONS);
        this.serverMap = redisClient.getLocalCachedMap(RedisConfig.SERVER_MAP.getKey(), DEFAULT_OPTIONS);
        this.patternMap = redisClient.getLocalCachedMap(RedisConfig.PATTERN_MAP.getKey(), DEFAULT_OPTIONS);

        this.initialized = true;
    }

    /**
     * Deletes all maps. We could do this with a {@link RBatch}, but is it really worth it for so
     * few maps? Secondly, we would've to list the maps again before deleting them via a batch, so we
     * just don't use it.
     */
    public void delete() {
        if(!initialized) return;
        groupMap.deleteAsync();
        playerMap.deleteAsync();
        nameUniqueIdMap.deleteAsync();
        configMap.deleteAsync();
        serverMap.deleteAsync();

        initialized = false;
    }

    /**
     * Gets a server out of the server map by using a predicate to indicate it
     *
     * @param predicate The predicate
     * @return The found server or null
     */
    public MooServer getServer(Predicate<MooServer> predicate) {
        for(MooServer s : getServerMap().readAllValues()) {
            if(predicate.test(s)) {
                return s;
            }
        }
        return null;
    }

    /**
     * Gets something from the config map
     *
     * @param type The type
     * @return The object
     */
    public <T> T getConfigEntry(NetworkConfigType type) {
        return (T) configMap.get(type.getKey());
    }

    public RFuture<Object> getConfigEntryAsync(NetworkConfigType type) {
        return configMap.getAsync(type.name().toLowerCase());
    }


}
