package de.superioz.moo.network.client;

import de.superioz.moo.network.server.NetworkServer;
import lombok.Getter;
import de.superioz.moo.api.collection.UnmodifiableList;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The hub for storing the client connections
 *
 * @see MooClient
 */
public final class ClientManager {

    /**
     * It is only necessary to only have one ClientHub instance so this is the static access for this
     * class after it has been initialised by the network server
     */
    @Getter
    private static ClientManager instance;

    /**
     * The connected {@link MooClient}'s by the type of them
     */
    private ConcurrentMap<ClientType, Map<InetSocketAddress, MooClient>> clientsByType = new ConcurrentHashMap<>();

    /**
     * The ram usage of every daemon (as socketaddress) as percent
     */
    @Getter
    private Map<InetSocketAddress, Integer> daemonRamUsage = new HashMap<>();

    /**
     * The netty server the clients are connected to
     */
    private NetworkServer netServer;

    public ClientManager(NetworkServer netServer) {
        instance = this;
        this.netServer = netServer;

        for(ClientType clientType : ClientType.values()) {
            clientsByType.put(clientType, new HashMap<>());
        }
    }

    /**
     * Updates the ramUsage of a daemon
     *
     * @param address  The address of the daemon/server
     * @param ramUsage The ramUsage in per cent
     */
    public void updateRamUsage(InetSocketAddress address, int ramUsage) {
        Map<InetSocketAddress, MooClient> daemonClients = clientsByType.get(ClientType.DAEMON);
        if(!daemonClients.containsKey(address)) return;
        daemonRamUsage.put(address, ramUsage);
    }

    /**
     * Gets the best available daemon where the ram usage is the lowest
     *
     * @return The client
     */
    public MooClient getBestDaemon() {
        Map<InetSocketAddress, MooClient> daemonClients = clientsByType.get(ClientType.DAEMON);
        if(daemonClients.isEmpty()) return null;

        int lowesRamUsage = -1;
        MooClient lowestRamUsageClient = null;

        for(InetSocketAddress address : daemonClients.keySet()) {
            if(!daemonRamUsage.containsKey(address)) continue;
            MooClient client = daemonClients.get(address);
            int ramUsage = daemonRamUsage.get(address);

            if((lowesRamUsage == -1 || lowesRamUsage > ramUsage)
                    && !((lowesRamUsage = ramUsage) >= (Integer) netServer.getConfig().get("slots-ram-usage"))) {
                lowestRamUsageClient = client;
            }
        }
        return lowestRamUsageClient;
    }

    /**
     * Adds a client to the hub
     *
     * @param cl The client
     * @return The size of the map
     */
    public int add(MooClient cl) {
        Map<InetSocketAddress, MooClient> map = clientsByType.get(cl.getType());
        map.put(cl.getAddress(), cl);

        if(cl.getType() == ClientType.DAEMON) {
            daemonRamUsage.put(cl.getAddress(), 0);
        }
        return map.size();
    }

    /**
     * Removes a client from the hub
     *
     * @param address The address (the key)
     * @return This
     */
    public ClientManager remove(InetSocketAddress address) {
        for(Map<InetSocketAddress, MooClient> m : clientsByType.values()) {
            m.entrySet().removeIf(entry -> entry.getKey().equals(address));
        }
        return this;
    }

    public ClientManager remove(MooClient cl) {
        return remove(cl.getAddress());
    }

    /**
     * Gets a client from address
     *
     * @param address The address
     * @return The client
     */
    public MooClient get(InetSocketAddress address) {
        MooClient client = null;
        for(Map.Entry<ClientType, Map<InetSocketAddress, MooClient>> entry : clientsByType.entrySet()) {
            if(entry.getValue().containsKey(address)) {
                client = entry.getValue().get(address);
            }
        }
        return client;
    }

    public boolean contains(InetSocketAddress address) {
        return get(address) != null;
    }

    /**
     * Get clients (from type)
     *
     * @param type The type
     * @return The list of clients (unmodifiable)
     */
    public UnmodifiableList<MooClient> getClients(ClientType type) {
        Map<InetSocketAddress, MooClient> map = clientsByType.get(type);

        return new UnmodifiableList<>(map.values());
    }

    /**
     * Get all clients inside one list
     *
     * @return The list of clients
     */
    public List<MooClient> getAll() {
        List<MooClient> clients = new ArrayList<>();
        for(ClientType clientType : ClientType.values()) {
            clients.addAll(getClients(clientType));
        }
        return clients;
    }

    public UnmodifiableList<MooClient> getMinecraftClients() {
        List<MooClient> clients = new ArrayList<>();
        clients.addAll(getClients(ClientType.PROXY));
        clients.addAll(getClients(ClientType.SERVER));

        return new UnmodifiableList<>(clients);
    }

    public UnmodifiableList<MooClient> getServerClients() {
        return getClients(ClientType.SERVER);
    }

    public UnmodifiableList<MooClient> getProxyClients() {
        return getClients(ClientType.PROXY);
    }

    public UnmodifiableList<MooClient> getCustomClients() {
        return getClients(ClientType.CUSTOM);
    }

    public UnmodifiableList<MooClient> getDaemonClients() {
        return getClients(ClientType.DAEMON);
    }

}
