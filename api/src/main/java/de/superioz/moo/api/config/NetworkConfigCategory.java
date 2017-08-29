package de.superioz.moo.api.config;

public enum NetworkConfigCategory {

    /**
     * Common settings (slots players, motd, ..)
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
