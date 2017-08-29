package de.superioz.moo.netty.server;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.common.Response;
import de.superioz.moo.netty.packets.PacketServerRequest;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Just a helper class for managing servers etc.
 * (Through the cache or through packets)
 *
 * @see MooCache
 * @see PacketMessenger
 */
public final class MooProxy {

    private static MooProxy instance;

    public static MooProxy getInstance() {
        if(instance == null) instance = new MooProxy();
        return instance;
    }

    /**
     * Get all servers currently active on this network
     *
     * @return The list of server
     */
    public List<MooServer> getServers() {
        return new ArrayList<>(MooCache.getInstance().getServerMap().readAllValues());
    }

    /**
     * Gets all servers currently active with given type
     *
     * @param type The type
     * @return The list of servers found
     * @see #getServers()
     */
    public List<MooServer> getServer(String type) {
        List<MooServer> servers = new ArrayList<>();
        getServers().forEach(mooServer -> {
            if(mooServer.getType().equalsIgnoreCase(type)) {
                servers.add(mooServer);
            }
        });
        return servers;
    }

    /**
     * Finds a server with given address (host + port)
     *
     * @param address The address to check for
     * @return The server or null
     */
    public MooServer getServer(InetSocketAddress address) {
        for(MooServer server : getServers()) {
            if(server.getAddress().equals(address)) return server;
        }
        return null;
    }

    public MooServer getServer(String host, int port) {
        return getServer(new InetSocketAddress(host, port));
    }

    /**
     * Get all patterns of this network out of the cache
     *
     * @return The list of patterns
     */
    public List<ServerPattern> getPatterns() {
        return new ArrayList<>(MooCache.getInstance().getPatternMap().readAllValues());
    }

    /**
     * Gets a pattern out of the {@link MooCache}
     *
     * @param type The name of the pattern
     * @return The server pattern
     */
    public ServerPattern getPattern(String type) {
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
    public void requestServer(String type, boolean autoSave, int amount, Consumer<Response> callback) {
        PacketMessenger.message(new PacketServerRequest(type, autoSave, amount), callback);
    }

}
