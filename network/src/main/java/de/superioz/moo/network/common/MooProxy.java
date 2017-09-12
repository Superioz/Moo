package de.superioz.moo.network.common;

import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.api.utils.CollectionUtil;
import de.superioz.moo.network.packets.PacketServerRequest;
import de.superioz.moo.network.packets.PacketServerRequestShutdown;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.network.queries.Response;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Just a helper class for managing servers etc.
 * (Through the cache or through packets)
 *
 * @see MooCache
 * @see PacketMessenger
 */
public final class MooProxy {

    public static final double UPPER_PLAYER_THRESHOLD = 7D / 10D;
    public static final double LOWER_PLAYER_THRESHOLD = 4D / 10D;

    /*
    =========================
    SERVERS
    =========================
     */

    /**
     * Get all servers currently active on this network
     *
     * @return The list of server
     */
    public static List<MooServer> getServers() {
        return new ArrayList<>(MooCache.getInstance().getServerMap().readAllValues());
    }

    /**
     * Gets all servers currently active with given type
     *
     * @param type The type
     * @return The list of servers found
     * @see #getServers()
     */
    public static List<MooServer> getServers(String type) {
        List<MooServer> servers = new ArrayList<>();
        getServers().forEach(mooServer -> {
            if(mooServer.getType().equalsIgnoreCase(type)) {
                servers.add(mooServer);
            }
        });
        return servers;
    }

    /**
     * Gets a server cluster for given type
     *
     * @param type The type of the server
     * @return The server cluster
     */
    public static MooServerCluster getCluster(String type) {
        ServerPattern pattern = getPattern(type);
        if(pattern == null) return null;

        return new MooServerCluster(pattern, getServers(type));
    }

    /**
     * Finds a server with given address (host + port)
     *
     * @param address The address to check for
     * @return The server or null
     */
    public static MooServer getServer(InetSocketAddress address) {
        for(MooServer server : getServers()) {
            if(server.getAddress().equals(address)) return server;
        }
        return null;
    }

    public static MooServer getServer(String host, int port) {
        return getServer(new InetSocketAddress(host, port));
    }

    /*
    =========================
    GROUP
    =========================
     */

    /**
     * Gets the group with given name
     *
     * @param name The group's name
     * @return The group
     */
    public static MooGroup getGroup(String name) {
        MooGroup group = MooCache.getInstance().getGroupMap().get(name);

        if(group == null) return new MooGroup(null);
        return group;
    }

    /**
     * Gets the group of the player but shifted {@code steps} steps either up or down
     *
     * @param currentGroup   The current group
     * @param steps          The steps to be shifted
     * @param up             Up or down the ladder? (true = up; false = down)
     * @param ignoreInfinite If the value '-1' should be ignored as infinite
     * @return The group
     */
    public static MooGroup getGroup(MooGroup currentGroup, int steps, boolean up, boolean ignoreInfinite) {
        if(steps < 0 && ignoreInfinite) steps *= -1;
        if(steps != -1) steps--;

        List<MooGroup> groupList = CollectionUtil.filterList(MooProxy.getGroups(),
                e -> up ? e.getRank() > currentGroup.getRank() : e.getRank() < currentGroup.getRank(),
                (o1, o2) -> o1.getRank().compareTo(o2.getRank()) * (up ? 1 : -1));

        // if the steps value is -1 (= infinite) then walk down/up the whole ladder
        if(!ignoreInfinite && steps == -1) {
            steps = groupList.size() - 1;
        }
        return CollectionUtil.getEntrySafely(groupList, steps, currentGroup);
    }

    public static MooGroup getGroup(MooGroup currentGroup, int steps, boolean up) {
        return getGroup(currentGroup, steps, up, false);
    }

    /**
     * Gets the list of all groups
     *
     * @return The list of moo groups
     */
    public static List<MooGroup> getGroups() {
        return new ArrayList<>(MooCache.getInstance().getGroupMap().values());
    }

    /**
     * Gets the list of all groups as raw groups
     *
     * @return The list of groups
     */
    public static List<Group> getRawGroups() {
        List<Group> groups = new ArrayList<>();
        getGroups().forEach(mooGroup -> groups.add(mooGroup.unwrap()));
        return groups;
    }

    /*
    =========================
    PLAYERS
    =========================
     */

    /**
     * Gets the moo player from this unique id
     *
     * @param uuid The unique id
     * @return The moo player
     */
    public static MooPlayer getPlayer(UUID uuid) {
        PlayerData data = MooCache.getInstance().getPlayerMap().get(uuid);

        // if data is null get offline player
        if(data == null) {
            data = MooQueries.getInstance().getPlayerData(uuid);
        }
        return data == null ? null : new MooPlayer(data);
    }

    /**
     * Gets the moo player from this name
     *
     * @param name The name of the player
     * @return The moo player
     */
    public static MooPlayer getPlayer(String name) {
        PlayerData data = null;
        for(PlayerData pd : MooCache.getInstance().getPlayerMap().readAllValues()) {
            if(pd.getLastName().equals(name)) {
                data = pd;
                break;
            }
        }
        if(data == null) {
            data = MooQueries.getInstance().getPlayerData(name);
        }
        return new MooPlayer(data);
    }

    /*
    =========================
    PATTERN
    =========================
     */

    /**
     * Get all patterns of this network out of the cache
     *
     * @return The list of patterns
     */
    public static List<ServerPattern> getPatterns() {
        return new ArrayList<>(MooCache.getInstance().getPatternMap().readAllValues());
    }

    /**
     * Gets a pattern out of the {@link MooCache}
     *
     * @param type The name of the pattern
     * @return The server pattern
     */
    public static ServerPattern getPattern(String type) {
        return MooCache.getInstance().getPatternMap().get(type);
    }

    /**
     * Requests a server to start with given values
     *
     * @param type     The type
     * @param autoSave Auto save after stop?
     * @param amount   Amount of servers to start
     * @param callback Callback after the
     * @ ..
     */
    public static void requestServer(String type, boolean autoSave, int amount, Consumer<Response> callback) {
        PacketMessenger.message(new PacketServerRequest(type, autoSave, amount), callback);
    }

    /**
     * Requests a server to shut down
     *
     * @param host     The host
     * @param port     The port
     * @param callback The callback
     */
    public static void requestServerShutdown(String host, int port, Consumer<Response> callback) {
        PacketMessenger.message(new PacketServerRequestShutdown(host, port), callback);
    }

    public static void requestServerShutdown(MooServer server, Consumer<Response> callback) {
        requestServerShutdown(server.getAddress().getHostName(), server.getAddress().getPort(), callback);
    }

    /*
    =========================
    OTHERS
    =========================
     */

    /**
     * This will be executed to test how many servers are started and therefore
     * eventually starts server etc.
     */
    public static void serverCycle(ServerPattern pattern) {
        // cannot check null pattern
        if(pattern == null) return;

        // get pattern cluster
        // if null, we can't do anything
        MooServerCluster serverCluster = getCluster(pattern.getName());
        if(serverCluster == null) return;

        // CHECK FOR TOO FEW SERVERS! (EITHER <min or players size is too much)
        int toOpen = serverCluster.needsToGetCycledForward(UPPER_PLAYER_THRESHOLD, LOWER_PLAYER_THRESHOLD);
        if(toOpen > 0) {
            // if bigger than max, cut down
            int total = serverCluster.getSize() + toOpen;
            int aboveLimit = total > pattern.getMax() ? total - pattern.getMax() : 0;
            if(aboveLimit > 0) toOpen = toOpen - aboveLimit;

            // if still after max check greater than zero
            if(toOpen > 0) {
                // send packet!
                requestServer(pattern.getName(), false, toOpen, null);
            }
        }

        // LETS CHECK IF WE CAN CLOSE A SERVER
        else if(serverCluster.needsToGetCycledBackward(LOWER_PLAYER_THRESHOLD)) {
            int count = 0;
            int maxCount = serverCluster.getSize() - pattern.getMin();

            // close empty servers (but not too much pliz)
            for(MooServer server : serverCluster.getServers()) {
                if(count == maxCount - 1) break;

                if(server.getOnlinePlayers() == 0) {
                    requestServerShutdown(server, null);
                }
            }
        }
    }

    public static void serverCycleAll() {
        MooCache.getInstance().getPatternMap().readAllValuesAsync()
                .thenAccept(serverPatterns -> serverPatterns.forEach(MooProxy::serverCycle));
    }


}
