package de.superioz.moo.protocol.client;

import de.superioz.moo.api.utils.BitMaskUtil;

public enum ClientType {

    /**
     * Don't know the client type
     */
    UNKNOWN,

    /**
     * A server instance (spigot/bukkit, ...)
     */
    SERVER,

    /**
     * A proxy instance (bungee, ...)
     */
    PROXY,

    /**
     * A daemon instance who manages server tasks
     */
    DAEMON,

    /**
     * Any other instance (maybe webserver?)
     */
    CUSTOM;

    /**
     * Applies this clientType onto given publishId
     *
     * @param publishId The publishId
     * @return The publishId with new flag (the clientType)
     */
    public int apply(int publishId) {
        int flag = BitMaskUtil.getFlag(ordinal());

        if(!BitMaskUtil.contains(publishId, flag)) {
            return BitMaskUtil.add(publishId, flag);
        }
        return publishId;
    }

}
