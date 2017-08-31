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

    /**
     * Gets an enum object by name from given enum without throwing a Nullpointer by doing it
     *
     * @param enumClass The enum class
     * @param name      The id of the enum
     * @return The enum object (or null)
     */
    public static Enum<?> getEnumByName(Class<? extends Enum<?>> enumClass, String name) {
        Object[] enumConstants = enumClass.getEnumConstants();
        for(Object enumConstant : enumConstants) {
            Enum<?> e = (Enum<?>) enumConstant;
            if(e.name().equalsIgnoreCase(name)) return e;
        }
        return null;
    }

}
