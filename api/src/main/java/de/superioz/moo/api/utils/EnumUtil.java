package de.superioz.moo.api.utils;

public final class EnumUtil {

    /**
     * Gets an enum object by id from given enum without throwing a Nullpointer by doing it
     *
     * @param enumClass The enum class
     * @param id        The id of the enum
     * @return The enum object (or null)
     */
    public static Enum<?> getEnumById(Class<? extends Enum<?>> enumClass, int id) {
        Object[] enumConstants = enumClass.getEnumConstants();
        int size = enumConstants.length;
        if(id < 0 || id >= size) return null;
        return (Enum<?>) enumConstants[id];
    }

}
