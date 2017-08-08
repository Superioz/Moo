package de.superioz.moo.api.io;

import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;

import java.util.ArrayList;

public enum MooConfigType {

    MOTD(""),
    PLAYER_COUNT(0),
    MAX_PLAYERS(0),
    MAINTENANCE(false),
    MAINTENANCE_MOTD(""),
    MAINTENANCE_RANK(0),
    PUNISHMENT_SUBTYPES(new ArrayList<>()),
    PUNISHMENT_REASONS(new ArrayList<>());

    @Getter
    private Object defaultValue;

    MooConfigType(Object defValue) {
        this.defaultValue = defValue;
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
