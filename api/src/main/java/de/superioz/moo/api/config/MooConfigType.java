package de.superioz.moo.api.config;

import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public enum MooConfigType {

    /**
     * The message of the day which is displayed in the server list
     */
    MOTD(MooConfigCategory.COMMON, "&aFirst line\n&2Second line"),

    /**
     * The maximum amount of players (displayed in the server list)
     */
    MAX_PLAYERS(MooConfigCategory.COMMON, 1337),

    /**
     * The switch of the maintenance mode
     */
    MAINTENANCE(MooConfigCategory.COMMON, false),

    /**
     * The motd if the maintenance is activated
     *
     * @see #MOTD
     */
    MAINTENANCE_MOTD(MooConfigCategory.COMMON, "&c1. Maintenance\n&42. Maintenance"),

    /**
     * The rank the player need to join during maintenance mode
     */
    MAINTENANCE_RANK(MooConfigCategory.COMMON, 10),

    /**
     * The punishment categories (clientmods, exploitation, ..)<br>
     * Format: name:time;type (e.g.: "clientmods:30d" or "chat-behaviour:6h;chat")
     */
    PUNISHMENT_CATEGORIES(MooConfigCategory.COMMON, new ArrayList<>()),

    /**
     * The subtypes of {@link #PUNISHMENT_CATEGORIES} (combat:clientmods, ..)<br>
     * Format: name:category (e.g.: "combat:clientmods" or "swearing:chat-behaviour")
     */
    PUNISHMENT_SUBTYPES(MooConfigCategory.COMMON, new ArrayList<>()),

    /**
     * The current amount of players
     */
    PLAYER_COUNT(MooConfigCategory.NONE, 0),;

    @Getter
    private Object defaultValue;

    @Getter
    private MooConfigCategory category;

    MooConfigType(MooConfigCategory category, Object defValue) {
        this.defaultValue = defValue;
        this.category = category;
    }

    /**
     * Gets all config types which category is the given one
     *
     * @param category The category
     * @return The list of types
     */
    public static List<MooConfigType> getConfigTypes(MooConfigCategory category) {
        List<MooConfigType> types = new ArrayList<>();
        for(MooConfigType mooConfigType : values()) {
            if(mooConfigType.category == category) types.add(mooConfigType);
        }
        return types;
    }

    /**
     * Gets the key of this type (just the name)
     *
     * @return The key as string
     */
    public String getKey() {
        return name().toLowerCase().replace("_", "-");
    }

    /**
     * Gets the given value by casting it to given class, if not successful return the default value
     *
     * @param val    The value to be casted
     * @param tClass The type class
     * @param <T>    The type
     * @return The casted value (or null)
     */
    public <T> T getValue(String val, Class<T> tClass) {
        T t = (T) ReflectionUtil.safeCast(val, tClass);
        if(t == null) return getDefaultValue().getClass().isAssignableFrom(tClass) ? (T) getDefaultValue() : null;
        return t;
    }

}
