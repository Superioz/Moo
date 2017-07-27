package de.superioz.moo.api.common;

import lombok.Getter;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

/**
 * A wrapper class for a server started by a daemon instance.<br>
 * This could be useful if the cloud wants to store every server with specific values
 */
@Getter
public class MooServer {

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
     * The online players (name:uuid)
     */
    private List<String> players;

    /**
     * This value is the timestamp of the last server update packet
     */
    private long lastUpdate;

    public MooServer(UUID uuid, InetSocketAddress address, String type) {
        this.uuid = uuid;
        this.address = address;
        this.type = type;
    }

    /**
     * Updates the server info
     *
     * @param motd          The message of the day
     * @param onlinePlayers The number of players online
     * @param maxPlayers    The maximum amount of players
     * @param players       The players online
     */
    public void updateInfo(String motd, int onlinePlayers, int maxPlayers, List<String> players) {
        this.motd = motd;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.players = players;

        // last update: now.
        this.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Pings the server and updates the motd and player values
     *
     * @throws Exception .
     */
    /*public void updatePing() throws Exception {
        Socket socket = new Socket();
        socket.connect(address);
        OutputStream out = socket.getOutputStream();
        InputStream in = socket.getInputStream();
        out.write(0xFE);

        int b;
        StringBuilder str = new StringBuilder();
        while((b = in.read()) != -1){
            if(b != 0 && b > 16 && b != 255 && b != 23 && b != 24) {
                str.append((char) b);
            }
        }

        String[] data = str.toString().split("§");
        data[0] = data[0].substring(1, data[0].length());

        this.motd = data[0];
        this.onlinePlayers = Integer.parseInt(data[1]);
        this.maxPlayers = Integer.parseInt(data[2]);
    }*/

}
