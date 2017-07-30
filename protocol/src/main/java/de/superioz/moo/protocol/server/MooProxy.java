package de.superioz.moo.protocol.server;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.collection.UnmodifiableList;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.Response;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.*;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Representation of the proxy where all clients are connected to
 */
public final class MooProxy {

    private final Map<UUID, PlayerData> playerMap = new HashMap<>();
    private final Map<String, PlayerData> playerNameMap = new HashMap<>();
    private final Map<UUID, InetSocketAddress> playerServerMap = new HashMap<>();

    private final Map<UUID, MooServer> spigotServerMap = new HashMap<>();

    private NetworkServer netServer;

    public MooProxy(NetworkServer netServer) {
        this.netServer = netServer;
    }

    /**
     * Get the daemon servers
     *
     * @return The map of daemon servers
     */
    public Map<UUID, MooServer> getSpigotServers() {
        return spigotServerMap;
    }

    /**
     * Gets the server out of the spigot server map where the address is the same
     *
     * @param address The address of the server
     * @return The moo server object
     */
    public MooServer getServer(InetSocketAddress address) {
        for(MooServer server : spigotServerMap.values()) {
            if(server.getAddress().equals(address)) return server;
        }
        return null;
    }

    /**
     * Requests server starts
     *
     * @param type     The type of server
     * @param autoSave Auto-save on shutdown?
     * @param amount   The amount of servers to start
     */
    public void requestServer(String type, boolean autoSave, int amount, Consumer<AbstractPacket> callback) {
        MooClient bestDaemon = netServer.getHub().getBestDaemon();
        if(bestDaemon == null) {
            callback.accept(new PacketRespond(ResponseStatus.NOT_FOUND));
            return;
        }

        PacketMessenger.create().target(bestDaemon)
                .send(new PacketServerRequest(type, autoSave, amount), (Consumer<AbstractPacket>) callback);
    }

    /**
     * Requests a shutdown for a server
     *
     * @param host     The host
     * @param port     The port
     * @param callback The callback
     */
    public void requestServerShutdown(String host, int port, Consumer<AbstractPacket> callback) {
        UnmodifiableList<MooClient> daemonClients = netServer.getHub().getDaemonClients();
        MooClient daemon = null;
        for(MooClient client : daemonClients) {
            if(client.getHost().equals(host)) {
                daemon = client;
                break;
            }
        }

        if(daemon == null) {
            callback.accept(new PacketRespond(ResponseStatus.NOT_FOUND));
            return;
        }

        PacketMessenger.create().target(daemon).send(new PacketServerRequestShutdown(host, port), callback);
    }

    /**
     * Oh no captain the ship is sinking!
     *
     * @param address The address
     */
    public void serverIsGoingDown(InetSocketAddress address) {
        playerServerMap.values().removeIf(address::equals);
    }

    /**
     * Gets all players currently connected
     *
     * @return The connected players
     */
    public Collection<PlayerData> getPlayers() {
        return playerMap.values();
    }

    /**
     * Gets the player with given uuid
     *
     * @param uuid of the player
     * @return their player instance
     */
    public PlayerData getPlayer(UUID uuid) {
        if(!contains(uuid)) return null;
        return playerMap.get(uuid);
    }

    public PlayerData getPlayer(String name) {
        if(!contains(name)) return null;
        return playerNameMap.get(name);
    }

    /**
     * Checks if the uuid is inside the map
     *
     * @param uuid The uniqueId
     * @return The result
     */
    public boolean contains(UUID uuid) {
        return playerMap.containsKey(uuid);
    }

    public boolean contains(String name) {
        return playerNameMap.containsKey(name);
    }

    /**
     * Adds a player to the map if not exists
     */
    public void add(PlayerData player, InetSocketAddress address) {
        if(!playerMap.containsKey(player.uuid)) {
            playerServerMap.put(player.uuid, address);
            playerMap.put(player.uuid, player);
            playerNameMap.put(player.lastName, player);
        }
    }

    /**
     * Removes a player to the map if exists
     */
    public void remove(UUID uuid, String name) {
        if(playerMap.containsKey(uuid)) {
            playerMap.remove(uuid);
            playerNameMap.remove(name);
            playerServerMap.remove(uuid);
        }
    }

    /**
     * Gets the mooClient of the player
     *
     * @param player The player
     * @return The mooClient
     */
    public MooClient getClient(PlayerData player) {
        InetSocketAddress address = playerServerMap.get(player.uuid);
        return netServer.getHub().get(address);
    }

    /**
     * Sends a message packets to the player's proxy
     *
     * @param player   The player
     * @param packet   The packets
     * @param callback The callback
     */
    public void sendMessage(PlayerData player, PacketPlayerMessage packet, Consumer<Response> callback) {
        InetSocketAddress address = playerServerMap.get(player.uuid);
        MooClient client = netServer.getHub().get(address);

        if(callback != null) {
            PacketMessenger.message(packet, callback, client);
        }
        else {
            PacketMessenger.message(packet, client);
        }
    }

    public void sendMessage(PlayerData player, PacketPlayerMessage packet) {
        sendMessage(player, packet, null);
    }

    public void sendMessage(PlayerData player, String message) {
        sendMessage(player, new PacketPlayerMessage(PacketPlayerMessage.Type.PRIVATE, message,
                "", true, true));
    }

    /**
     * Kicks the MooPlayer
     *
     * @param player   The player
     * @param packet   The packets
     * @param callback The callback
     */
    public void kick(PlayerData player, PacketPlayerKick packet, Consumer<Response> callback) {
        PacketMessenger.message(packet, callback, getClient(player));
    }

}
