package de.superioz.moo.api.redis;

public enum RedisConfig {

    /**
     * @see MooCache#groupMap
     */
    GROUP_MAP,

    /**
     * @see MooCache#playerMap
     */
    PLAYER_MAP,

    /**
     * @see MooCache#nameUniqueIdMap
     */
    PLAYER_ID_MAP,

    /**
     * @see MooCache#playerPermissionMap
     */
    PLAYER_PERMISSION_MAP,

    /**
     * @see MooCache#configMap
     */
    CONFIG_MAP,

    /**
     * @see MooCache#serverMap
     */
    SERVER_MAP,

    /**
     * @see MooCache#patternMap
     */
    PATTERN_MAP;

    RedisConfig() {
    }

    /**
     * Gets the key of the map for cache storing
     *
     * @return The key as string
     */
    public String getKey() {
        return name().toLowerCase();
    }

}
