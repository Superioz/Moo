package de.superioz.moo.api.config;

public enum MooConfigCategory {

    /**
     * Common settings (max players, motd, ..)
     */
    COMMON,

    /**
     * Settings which are not in the database
     */
    NONE;

    /**
     * Simply the name to lower case
     *
     * @return The name
     */
    public String getName() {
        return name().toLowerCase();
    }

}
