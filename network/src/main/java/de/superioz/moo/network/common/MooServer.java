package de.superioz.moo.network.common;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.objects.ServerPattern;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A wrapper class for a server started by a daemon instance.<br>
 * This could be useful if the cloud wants to store every server with specific values
 */
@Getter
public class MooServer {

    public static final char SERVER_SPLIT = '-';

    /**
     * Unique id of the server instance
     */
    private UUID uuid;

    /**
     * Address of the server
     */
    private InetSocketAddress address;

    /**
     * Type of the server (could be "lobby" or "skyblock", ...)
     */
    private String type;

    /**
     * The id of the server (e.g.: 1 for lobby-1)
     */
    private int id;

    /**
     * The server pattern this server was created from
     */
    private ServerPattern pattern;

    /**
     * Current motd of the server
     */
    private String motd;

    /**
     * Current size of online players on this instance
     */
    private int onlinePlayers;

    /**
     * Maximum amount of players that can be online at the same time on this server
     */
    private int maxPlayers;

    /**
     * This value is the timestamp of the last server update packet
     */
    private long lastHeartBeat = -1;

    public MooServer(ServerPattern pattern, int id, InetSocketAddress address, String type) {
        this.pattern = pattern;
        this.id = id;
        this.address = address;
        this.type = type;
        this.uuid = UUID.nameUUIDFromBytes((type + "#" + id).getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Gets the name of the server (for registering in bungee)
     *
     * @return The name
     */
    public String getName() {
        return type + SERVER_SPLIT + id;
    }

    /**
     * Get all players online on this server
     *
     * @return The list of players
     */
    public List<PlayerData> getPlayers() {
        List<PlayerData> players = new ArrayList<>();
        MooCache.getInstance().getPlayerMap().values().forEach(playerData -> {
            if(playerData.getCurrentServer().equals(getName())) players.add(playerData);
        });
        return players;
    }

    /**
     * Updates the server info
     *
     * @param motd          The message of the day
     * @param onlinePlayers The number of players online
     * @param maxPlayers    The maximum amount of players
     */
    public void updateInfo(String motd, int onlinePlayers, int maxPlayers) {
        this.motd = motd;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;

        // update in cache
        MooCache.getInstance().getServerMap().putAsync(getUuid(), this);
    }

    /**
     * Lets the heart beat of this server (for timeout purposes?)
     */
    public void heartbeat() {
        this.lastHeartBeat = System.currentTimeMillis();

        // update in cache
        MooCache.getInstance().getServerMap().putAsync(getUuid(), this);
    }

}
