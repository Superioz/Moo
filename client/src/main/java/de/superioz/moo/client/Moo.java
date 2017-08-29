package de.superioz.moo.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.cache.RedisConnection;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.exceptions.InvalidConfigException;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.client.exception.MooInitializationException;
import de.superioz.moo.client.listeners.QueryClientListener;
import de.superioz.moo.client.paramtypes.GroupParamType;
import de.superioz.moo.client.paramtypes.PlayerDataParamType;
import de.superioz.moo.client.paramtypes.PlayerInfoParamType;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.netty.client.NetworkClient;
import de.superioz.moo.netty.common.*;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketAdapting;
import de.superioz.moo.netty.packets.PacketConfig;
import de.superioz.moo.netty.packets.PacketPing;
import de.superioz.moo.netty.packets.PacketPlayerMessage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * This class is for connecting to the cloud as client
 *
 * @see RedisConnection
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Moo {

    /**
     * Name of the config file per default
     * (if you want to load a config without thinking of a name)
     */
    public static final String CONFIG_DEFAULT_NAME = "config";

    /**
     * The activation field inside the config for checking if the cloud
     * should be activated or deactivated
     */
    public static final String CLOUD_ACTIVATION_CONFIG = "cloud";

    private static Moo instance;

    public static synchronized Moo getInstance() {
        if(instance == null) {
            instance = new Moo();
        }
        return instance;
    }

    /**
     * The executor service for async processing
     */
    private ExecutorService executors
            = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("moo-pool-%d").build());

    /**
     * The network client for the connection to the cloud
     */
    private NetworkClient client;

    /**
     * The name of the client (e.g. skyblock, ..)
     */
    private String clientName;

    /**
     * The type of the client
     */
    private ClientType clientType;

    /**
     * The subport. Either -1 or the port of the spigot server
     */
    @Setter
    private int subPort = -1;

    /**
     * The logger of the client
     */
    @Setter
    private Logger logger;

    @Setter private boolean enabled = true;
    @Setter private boolean autoReconnect = true;

    /**
     * The default parameter types
     */
    private static final ParamType[] DEFAULT_PARAM_TYPES = new ParamType[]{
            new PlayerDataParamType(), new PlayerInfoParamType(), new GroupParamType()
    };

    static {
        EventExecutor.getInstance().register(new QueryClientListener());
        CommandRegistry.getInstance().getParamTypeRegistry().register(DEFAULT_PARAM_TYPES);
    }

    /**
     * Initialises the moo instance<br>
     * It will create the {@link Moo} instance, initialize the logger and database
     */
    public static void initialize(Logger logger) {
        // create new instance
        if(instance == null) {
            getInstance();
        }

        // reinitialize the values
        if(logger != null) instance.logger = logger;
    }

    /**
     * Executes given runnable asynchronous
     *
     * @param r The runnable
     */
    public void runAsync(Runnable r) {
        executors.execute(r);
    }

    /**
     * Executes given runnable asynchronous and waits for a result
     *
     * @param r   The runnable as callable
     * @param <V> The type of result
     * @return The result
     */
    public <V> V runAsync(Callable<V> r) {
        Future<V> future = executors.submit(r);

        try {
            return future.get(60, TimeUnit.SECONDS);
        }
        catch(Exception e) {
            System.err.println("Error while waiting for " + future.toString() + " to finish: ");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Starts the netclient. If moo is already connected nothing will happen.
     * Otherwise will the {@link NetworkClient} be initialised. If the client is already
     * initialised then Moo will just connect.<br>
     *
     * @param host The host
     * @param port The port
     */
    public void connect(String clientName, ClientType clientType, String host, int port) {
        if(!isEnabled()) return;
        getLogger().info("Initialising cloud-connection ..");

        this.clientName = clientName;
        this.clientType = clientType;

        executors.execute(() -> {
            try {
                // is the client already initialised?
                if(client == null) {
                    client = new NetworkClient(host, port, getLogger());
                    client.registerEventAdapter(new MooNetworkAdapter(this));
                    client.setup();
                }
                client.connect();
            }
            catch(Exception e) {
                // failed to connect
                getLogger().warning("Couldn't connect to master server! Is the cloud down?");

                /*// made every plugin prepare
                getPluginManager().onStateChange();*/
            }
        });
        autoReconnect = true;
    }

    /**
     * Stops the netclient
     *
     * @see NetworkClient
     */
    public void disconnect() {
        autoReconnect = false;
        client.disconnect();
    }

    /**
     * Reconnects the client (either direct connecting or first disconnecting)
     */
    public void reconnect() {
        if(isConnected()) {
            this.disconnect();
        }
        this.connect(clientName, clientType, getClient().getHost(), getClient().getPort());
    }

    /**
     * Checks if the client is connected
     * (thats just the simply connection state, after that the client needs to be authorized)
     *
     * @return The result
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Checks if the client is authenticated (happens after connecting)
     *
     * @return The result
     * @see #isConnected()
     */
    public boolean isAuthenticated() {
        return client != null && client.isAuthenticated();
    }

    /**
     * Checks if Moo is initialized, that means checking if the instance is null, if it is
     * throw an exception, if not check if it is activated
     *
     * @return The result
     */
    public boolean check() {
        if(instance == null) throw new MooInitializationException();
        return isEnabled();
    }

    /**
     * Waiting for the client to be authenticated
     */
    public void waitForAuthentication() {
        while(!isAuthenticated()){
            try {
                Thread.sleep(5L);
            }
            catch(InterruptedException e) {
                //
            }
        }
    }

    /**
     * Simply waiting for the client to list disconnected
     */
    public void waitForShutdown(){
        while(isConnected()){
            try {
                Thread.sleep(25L);
            }
            catch(InterruptedException e) {
                //
            }
        }
    }

    /*
    ============================================
    SPECIAL METHODS >> PLUGIN METHODS
    ============================================
     */

    /**
     * Loads a config file for you (Only .json). If you want to use yaml use
     * spigot or bungee.
     *
     * @param folder The folder where the config should be placed inside
     * @param name   The name of the file (without suffix)
     * @return The config
     */
    public JsonConfig loadConfig(File folder, String name) {
        // check for activation
        check();

        // load configuration
        getLogger().info("Loading configuration ..");
        JsonConfig config = new JsonConfig(name, folder);
        config.load(true, true);

        // check for cloud activation
        if(config.isLoaded()) {
            try {
                Moo.getInstance().setEnabled(config.get(CLOUD_ACTIVATION_CONFIG));
            }
            catch(InvalidConfigException ex) {
                // do nothing, true is default anyway
            }
        }

        return config;
    }

    public JsonConfig loadConfig(File folder) {
        return loadConfig(folder, CONFIG_DEFAULT_NAME);
    }

    /**
     * Registers all handlers within given object array<br>
     * This method checks for the classes who either implements {@link EventListener} or {@link PacketAdapter}<br>
     * If you want to check for custom class instances use the custom check consumer.
     *
     * @param classes The classes to be registered
     */
    public void registerHandler(Consumer<Object> customCheckConsumer, Object... classes) {
        boolean customCheck = customCheckConsumer != null;

        // go through classes
        for(Object listenerClass : classes) {
            if(listenerClass instanceof EventListener) EventExecutor.getInstance().register((EventListener) listenerClass);
            if(listenerClass instanceof PacketAdapter) PacketAdapting.getInstance().register((PacketAdapter) listenerClass);
            if(customCheck) customCheckConsumer.accept(listenerClass);
        }
    }

    public void registerHandler(Object... classes) {
        registerHandler(null, classes);
    }

    /*
    ===============================================
    SPECIAL METHODS >> PACKET METHODS
    ===============================================
     */

    /**
     * Broadcasts a message across the network
     *
     * @param message The message
     * @return The status
     */
    public ResponseStatus broadcast(String message) {
        return MooQueries.getInstance().sendMessage(PacketPlayerMessage.Type.BROADCAST, message, "");
    }

    public ResponseStatus broadcast(String message, String permission) {
        return MooQueries.getInstance().sendMessage(PacketPlayerMessage.Type.RESTRICTED_PERM, message, permission);
    }

    /**
     * This broadcasting will send a message via {@link #broadcast(String)} across the network to all
     * players whose rank is >= given rank. This could be interpreted to a team chat
     *
     * @param message The message
     * @param rank    The rank
     * @return The status
     */
    public ResponseStatus broadcast(String message, int rank) {
        return MooQueries.getInstance().sendMessage(PacketPlayerMessage.Type.RESTRICTED_RANK, message, rank + "");
    }

    public ResponseStatus broadcast(String message, int rank, boolean colored, boolean formatted) {
        return MooQueries.getInstance().sendMessage(PacketPlayerMessage.Type.RESTRICTED_RANK, message, rank + "", colored, formatted);
    }

    /**
     * Sends a config packets
     *
     * @param type     The type
     * @param metadata The metadata
     * @return The respond
     */
    public ResponseStatus config(NetworkConfigType type, String metadata) {
        return PacketMessenger.<Response>transfer(new PacketConfig(type, metadata), ResponseScope.RESPONSE).getStatus();
    }

    /**
     * Pings the cloud
     *
     * @return The ping as int
     */
    public int ping() {
        PacketPing packet = PacketMessenger.transfer(new PacketPing(), PacketPing.class);
        if(packet == null) return -1;

        return (int) (System.currentTimeMillis() - packet.timestamp);
    }

}
