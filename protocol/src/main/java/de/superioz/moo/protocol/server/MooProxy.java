package de.superioz.moo.protocol.server;

import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.common.MooPlayer;
import de.superioz.moo.api.collection.UnmodifiableList;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.common.Response;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.*;
import net.draxento.protocol.packets.*;

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

    private final Map<UUID, MooPlayer> playerMap = new HashMap<>();
    private final Map<String, MooPlayer> playerNameMap = new HashMap<>();
    private final Map<UUID, InetSocketAddress> serverMap = new HashMap<>();
    private final Map<UUID, MooServer> daemonServerMap = new HashMap<>();

    private NetworkServer netServer;

    public MooProxy(NetworkServer netServer) {
        this.netServer = netServer;
    }

    /**
     * Get the daemon servers
     *
     * @return The map of daemon servers
     */
    public Map<UUID, MooServer> getDaemonServers() {
        return daemonServerMap;
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
                .send(new PacketServerRequest(type, autoSave, amount), callback);
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
        serverMap.values().removeIf(address::equals);
    }

    /**
     * Gets all players currently connected
     *
     * @return The connected players
     */
    public Collection<MooPlayer> getPlayers() {
        return playerMap.values();
    }

    /**
     * Gets the player with given uuid
     *
     * @param uuid of the player
     * @return their player instance
     */
    public MooPlayer getPlayer(UUID uuid) {
        if(!contains(uuid)) return null;
        return playerMap.get(uuid);
    }

    public MooPlayer getPlayer(String name) {
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
    public void add(MooPlayer player, InetSocketAddress address) {
        if(!playerMap.containsKey(player.uuid)) {
            serverMap.put(player.uuid, address);
            playerMap.put(player.uuid, player);
            playerNameMap.put(player.name, player);
        }
    }

    /**
     * Removes a player to the map if exists
     */
    public void remove(UUID uuid, String name) {
        if(playerMap.containsKey(uuid)) {
            playerMap.remove(uuid);
            playerNameMap.remove(name);
            serverMap.remove(uuid);
        }
    }

    /**
     * Gets the mooClient of the player
     *
     * @param player The player
     * @return The mooClient
     */
    public MooClient getClient(MooPlayer player) {
        InetSocketAddress address = serverMap.get(player.uuid);
        return netServer.getHub().get(address);
    }

    /**
     * Sends a message packets to the player's proxy
     *
     * @param player   The player
     * @param packet   The packets
     * @param callback The callback
     */
    public void sendMessage(MooPlayer player, PacketPlayerMessage packet, Consumer<Response> callback) {
        InetSocketAddress address = serverMap.get(player.uuid);
        MooClient client = netServer.getHub().get(address);

        if(callback != null) {
            PacketMessenger.message(packet, callback, client);
        }
        else {
            PacketMessenger.message(packet, client);
        }
    }

    public void sendMessage(MooPlayer player, PacketPlayerMessage packet) {
        sendMessage(player, packet, null);
    }

    public void sendMessage(MooPlayer player, String message) {
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
    public void kick(MooPlayer player, PacketPlayerKick packet, Consumer<Response> callback) {
        PacketMessenger.message(packet, callback, getClient(player));
    }

}
