package de.superioz.moo.api.common;

import de.superioz.moo.api.util.SimpleSerializable;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Wrapper class for a player online on the server<br>
 * This object is stored inside the cloud to determine important things during the online time of the player
 */
@NoArgsConstructor
public class MooPlayer extends SimpleSerializable {

    public UUID uuid;
    public String name;
    public String ip;
    public Integer proxyId;
    public String currentServer;

    public MooPlayer(UUID uuid, String name, String ip, Integer proxyId, String currentServer) {
        this.uuid = uuid;
        this.name = name;
        this.ip = ip;
        this.proxyId = proxyId;
        this.currentServer = currentServer;
    }

}
