package de.superioz.moo.api.common;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.objects.ServerPattern;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * A cluster of one server type's servers
 */
public class MooServerCluster {

    @Getter
    private ServerPattern pattern;

    @Getter
    private List<MooServer> servers;

    public MooServerCluster(ServerPattern pattern, List<MooServer> servers) {
        this.pattern = pattern;
        this.servers = servers;
    }

    /**
     * Get all players who are online on this cluster
     *
     * @return The list of players
     */
    public List<PlayerData> getOnlinePlayers() {
        List<PlayerData> players = new ArrayList<>();
        servers.forEach(server -> players.addAll(server.getPlayers()));
        return players;
    }

    /**
     * Maximum amount of players online on this cluster (Σ-Function of all
     *
     * @return The max amount
     */
    public int getMaxPlayers() {
        int count = 0;
        for(MooServer server : servers) {
            count += server.getPattern().getMax();
        }
        return count;
    }

    /**
     * Gets the amount of players/max players in percent
     *
     * @return The percentage as double
     */
    public double getPlayersPercent() {
        return (double) (getOnlinePlayers().size() / getMaxPlayers());
    }

    /**
     * Returns the size of the cluster
     *
     * @return The size as int
     */
    public int getSize() {
        return servers.size();
    }

    /**
     * Checks if the cluster needs to be cycled forward which means to start server because
     * of too few servers only atm (Either <min servers or too much players)
     *
     * @return The result (0 = no need; >0 = start server)
     */
    public int needsToGetCycledForward(double thresholdTotal, double thresholdServer) {
        // CHECK FOR MINIMUM ON SERVERS
        if(getSize() < pattern.getMin()) {
            return getSize() - pattern.getMin();
        }

        // LETS CHECK IF THE PLAYERS SIZE IS 2/3+
        double playersPercent = getPlayersPercent();
        if(playersPercent > thresholdTotal) {
            // there are many players, but every server needs at least 3/7 fill
            int count = 0;
            for(MooServer server : getServers()) {
                if((double) (server.getOnlinePlayers() / server.getMaxPlayers()) < thresholdServer) count++;
            }

            // if every server is at least thresholdServer filled then ..
            // create a new server!
            if(count > 0) return 1;
        }
        return 0;
    }

    /**
     * Checks if the cluster needs to be cycled backward which means to stop empty servers
     *
     * @return The amount of servers to close (all empty servers)
     */
    public boolean needsToGetCycledBackward(double threshold) {
        // if players under the threshold are online and if more than min pattern servers are online
        return getPlayersPercent() < threshold
                && getSize() > pattern.getMin();
    }


}
