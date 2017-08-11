package de.superioz.moo.cloud;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.console.CommandTerminal;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.CustomFile;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Loogger;
import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.api.module.ModuleRegistry;
import de.superioz.moo.api.modules.RedisModule;
import de.superioz.moo.cloud.modules.*;
import de.superioz.moo.cloud.task.ServerInfoCheckTask;
import de.superioz.moo.protocol.server.ClientManager;
import de.superioz.moo.protocol.server.MooProxy;
import de.superioz.moo.protocol.server.NetworkServer;
import jline.console.ConsoleReader;
import lombok.Getter;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Cloud implements EventListener {

    @Getter
    private static Cloud instance;
    @Getter
    private Loogger logger;

    private final ExecutorService executors = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("cloud-pool-%d").build());
    private ConsoleReader reader;
    private MooProxy mooProxy;
    private CommandTerminal commandTerminal;

    private ModuleRegistry moduleRegistry;
    private ConfigModule configModule;
    private CommandModule commandModule;
    private DatabaseModule databaseModule;
    private RedisModule redisModule;
    private NettyModule nettyModule;
    private ListenerModule listenerModule;

    private boolean started = false;
    private ServerInfoCheckTask serverInfoCheckTask;

    /**
     * The main method
     *
     * @param args /
     */
    public static void main(String[] args) {
        Cloud cloud = new Cloud();

        cloud.start();
    }

    public Cloud() {
        instance = this;

        // initialises the console things
        MooLogger logger = new MooLogger("Moo");
        reader = logger.getReader();
        this.logger = new Loogger(logger).enableFileLogging();
        this.logger.prepareNativeStreams();
    }

    /**
     * Starts the cloud and therefore everything that needs to be started too.
     * The cloud uses the module system, so everything big will start as module
     * so the cloud processes are split in different logical threads
     */
    public void start() {
        getLogger().info("** Starting cloud v" + getVersion() + " .. (From " + Paths.get("").toAbsolutePath() + ") **");
        this.moduleRegistry = new ModuleRegistry(logger);
        moduleRegistry.setService(executors);

        // modules
        this.listenerModule = moduleRegistry.register(new ListenerModule());
        this.commandModule = moduleRegistry.register(new CommandModule());
        this.configModule = moduleRegistry.register(new ConfigModule(this));
        this.configModule.waitFor(module -> {
            if(module.getErrorReason() != null) return;

            // start redis
            CustomFile customFile = new CustomFile(((ConfigModule) module).getConfig().get("redis-config"), Paths.get("configuration"));
            customFile.load(true, true);
            moduleRegistry.register(redisModule = new RedisModule(customFile.getFile(), getLogger().getBaseLogger()));
        });
        this.databaseModule = moduleRegistry.register(new DatabaseModule(getConfig()));
        this.nettyModule = moduleRegistry.register(new NettyModule(getConfig()));
        this.nettyModule.waitForAsync(module -> {
            Cloud.this.mooProxy = new MooProxy(getServer());
            executors.execute(serverInfoCheckTask = new ServerInfoCheckTask(10 * 1000, 20 * 1000));
        });

        // send module summary
        getLogger().info("Finished initializing modules. Let's see how successful we were ..");
        moduleRegistry.sendModuleSummaryAsync();

        // commands
        this.commandTerminal = new CommandTerminal();
        this.commandTerminal.start(true, logger, reader);

        // finished
        getLogger().info("Finished starting Cloud! It is recommended to wait 5s before trying to connect.");
        executors.execute(() -> {
            try {
                Thread.sleep(5 * 1000);
                if(nettyModule.isEnabled()) {
                    getLogger().info("Netty instances should now be able to connect successfully."
                        + ((!databaseModule.isEnabled() || !redisModule.isEnabled()) ? " BUT BE CAUTIOUS - it seems the database/redis is not enabled!" : ""));
                    started = true;
                }
            }
            catch(InterruptedException e) {
                //
            }
        });
    }

    /**
     * Stops the cloud and therefore every module etc.
     */
    public void stop() {
        started = false;

        getLogger().info("Stopping cloud ..");
        MooCache.getInstance().delete();
        moduleRegistry.disableAll();
        executors.shutdownNow();

        this.commandTerminal.stop();
        logger.disable();

        System.exit(0);
    }

    /**
     * Gets the config from the config module
     *
     * @return The config object
     */
    public JsonConfig getConfig() {
        return configModule.getConfig();
    }

    /**
     * Gets the databaseCollection with given type
     *
     * @param type The type
     * @return The collection
     */
    public <T extends DatabaseCollection> T getDatabaseCollection(DatabaseType type) {
        Object coll = getDatabaseModule().getCollection(type);
        if(coll == null) throw new NullPointerException("Couldn't find database collection for " + type);
        return (T) coll;
    }

    /**
     * Gets the version of the cloud
     *
     * @return The version string
     */
    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    /*
    ==========================
    GETTER
    ==========================
     */

    public NetworkServer getServer() {
        return nettyModule == null ? null : nettyModule.getServer();
    }

    public ClientManager getClientManager() {
        return getServer() == null ? null : getServer().getClientManager();
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseModule.getDbConn();
    }

}
