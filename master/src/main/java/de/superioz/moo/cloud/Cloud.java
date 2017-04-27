package de.superioz.moo.cloud;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.cloud.modules.*;
import jline.console.ConsoleReader;
import lombok.Getter;
import de.superioz.moo.api.console.CommandTerminal;
import de.superioz.moo.api.database.DatabaseCollection;
import de.superioz.moo.api.database.DatabaseConnection;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Logs;
import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.api.module.ModuleRegistry;
import net.draxento.cloud.modules.*;
import de.superioz.moo.protocol.server.ClientHub;
import de.superioz.moo.protocol.server.MooProxy;
import de.superioz.moo.protocol.server.NetworkServer;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Cloud implements EventListener {

    @Getter
    private static Cloud instance;
    @Getter
    private static Logs logger;

    private final ExecutorService executors = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("cloud-pool-%d").build());
    private JsonConfig config;
    private ConsoleReader reader;
    private MooProxy mooProxy;
    private CommandTerminal commandTerminal;

    private ModuleRegistry moduleRegistry;
    private ConfigModule configModule;
    private CommandModule commandModule;
    private DatabaseModule databaseModule;
    private NettyModule nettyModule;
    private ListenerModule listenerModule;

    private boolean started = false;

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
        Cloud.logger = new Logs(logger).enableFileLogging();
        Cloud.logger.prepareNativeStreams();
    }

    /**
     * Starts the cloud and therefore everything that needs to be started too.
     * The cloud uses the module system, so everything big will start as module
     * so the cloud processes are split in different logical threads
     */
    public void start() {
        getLogger().info("Starting cloud v" + getVersion() + " .. (From " + Paths.get("").toAbsolutePath() + ")");
        this.moduleRegistry = new ModuleRegistry(logger);
        moduleRegistry.setService(executors);

        // modules
        this.listenerModule = moduleRegistry.register(new ListenerModule());
        this.commandModule = moduleRegistry.register(new CommandModule());
        this.configModule = moduleRegistry.register(new ConfigModule(this));
        this.config = configModule.getConfig();
        this.databaseModule = moduleRegistry.register(new DatabaseModule(config));
        this.nettyModule = moduleRegistry.register(new NettyModule(config));
        this.mooProxy = new MooProxy(getServer());

        // send module summary
        getLogger().info("Finished initializing modules.");
        moduleRegistry.sendModuleSummary();

        // commands
        this.commandTerminal = new CommandTerminal();
        this.commandTerminal.start(true, logger, reader);

        // finished
        getLogger().info("Finished starting Cloud! It is recommended to wait 5s before trying to connect.");
        executors.execute(() -> {
            try {
                Thread.sleep(5 * 1000);
                getLogger().info("Now should netty instances be able to connect successfully.");
                started = true;
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
        moduleRegistry.disableAll();
        executors.shutdownNow();

        this.commandTerminal.stop();
        logger.disable();
        System.exit(0);
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
        return nettyModule.getServer();
    }

    public ClientHub getHub() {
        return getServer().getHub();
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseModule.getDbConn();
    }

}
