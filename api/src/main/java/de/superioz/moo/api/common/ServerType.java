package de.superioz.moo.api.common;

public enum ServerType {

    LOBBY,
    GAME,
    OTHER;

    /**
     * Returns the server type from given name
     *
     * @param n The name
     * @return The type of the server
     */
    public static ServerType fromName(String n) {
        for(ServerType t : values()) {
            if(t.name().equalsIgnoreCase(n)) return t;
        }
        return ServerType.OTHER;
    }

}
