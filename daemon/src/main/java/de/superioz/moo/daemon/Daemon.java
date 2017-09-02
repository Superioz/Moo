package de.superioz.moo.daemon;

import de.superioz.moo.network.redis.MooCache;
import de.superioz.moo.api.console.CommandTerminal;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.RedisConnectionEvent;
import de.superioz.moo.api.io.CustomFile;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.ExtendedLogger;
import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.api.module.ModuleRegistry;
import de.superioz.moo.api.modules.RedisModule;
import de.superioz.moo.api.utils.SystemUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.task.ServerStartTask;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Getter
public class Daemon implements EventListener {

    public static final Pattern PREDEFINED_SERVER_PATTERN = Pattern.compile("\\w+(:\\d+)?");

    @Getter
    private static Daemon instance;

    public DaemonInstance server;

    public JsonConfig config;
    private ExtendedLogger logs;

    public ModuleRegistry moduleRegistry;
    public DaemonModule daemonModule;
    public RedisModule redisModule;


    /**
     * Checks if the daemon is connected, easily by checking if the Moo instance is connected
     *
     * @return The result
     */
    public boolean isConnected() {
        return Moo.getInstance().isConnected();
    }

    /**
     * Simply closes every server started by this instance
     *
     * @param stoppingServerConsumer The consumer for logging etc.
     */
    public void closeEveryServer(Consumer<Server> stoppingServerConsumer) {
        for(Server server : Daemon.getInstance().getServer().getStartedServerByUuid().values()) {
            if(stoppingServerConsumer != null) stoppingServerConsumer.accept(server);
            server.stop();
        }
    }

    /**
     * Starts one or multiple server with given values
     *
     * @param type        The type of the server (e.g. lobby)
     * @param ram         Max amount of ram
     * @param autoSave    If the server should auto save or just list deleted after shutdown
     * @param amount      The amount of this type of server to start
     * @param resultOfReq The result of the request (Server started or rip)
     */
    public void startServer(String type, String ram, boolean autoSave, int amount, Consumer<Server> resultOfReq) {
        for(int i = 0; i < amount; i++) {
            ServerStartTask task = new ServerStartTask(type, -1, ram, autoSave, resultOfReq);
            Daemon.getInstance().getServer().getServerQueue().getQueue().offer(task);
        }
    }

    public Daemon() {
    }

    private void init() {
        MooLogger logger = new MooLogger("Daemon");
        this.logs = new ExtendedLogger(logger).enableFileLogging().prepareNativeStreams();

        logs.warning("*** STOPPING THE DAEMON FORCEFULLY RESULTS IN GHOST PROCESSES  ***");
        logs.warning("*** IT IS RECOMMENDED TO USE THE 'STOP' COMMAND BEFORE CLOSING ***");
        logs.info("Start daemon @" + System.getProperty("user.dir") + " (" + (SystemUtil.isWindows() ? "WINDOWS" : "LINUX") + ")");
        Moo.initialize(logger);

        // modules
        this.moduleRegistry = new ModuleRegistry(logs);
        this.moduleRegistry.register(daemonModule = new DaemonModule(this));
        daemonModule.waitFor(module -> {
            if(module.getErrorReason() != null) return;
            CustomFile customFile = new CustomFile((config.get("redis-config")), Paths.get("configuration"));
            customFile.load(true, true);
            moduleRegistry.register(new RedisModule(customFile.getFile(), logs.getBaseLogger()));
        });

        // start the command terminal for fancy console input
        new CommandTerminal().start(true, getInstance().getLogs(), logger.getReader());

        // send summary
        moduleRegistry.sendModuleSummaryAsync();
    }

    /**
     * The main method of the daemon
     *
     * @param args .
     */
    public static void main(String[] args) {
        instance = new Daemon();
        instance.init();
    }

    @EventHandler
    public void onRedis(RedisConnectionEvent event) {
        if(event.isConnectionActive()) return;
        MooCache.getInstance().getPatternMap().readAllValuesAsync()
                .thenAccept(serverPatterns -> serverPatterns.forEach(pattern -> getServer().createPattern(pattern.getName())));
    }

    @EventHandler
    public void onCloudConnect(CloudConnectedEvent event) {
        logs.info("** AUTHENTICATION STATUS: " + (event.getStatus().getColored()) + " **");

        // create daemon instance
        server = new DaemonInstance(new File((String) config.get("patterns-folder")),
                new File((String) config.get("servers-folder")), config.get("start-file")).fetchPatterns();
        server.createFolders();

        // delete old servers
        logs.debug("Delete old servers ..");
        try {
            int oldServerCount = getInstance().getServer().cleanupServers();
            logs.debug("Server folder cleaned. (" + oldServerCount + "x)");
        }
        catch(IOException e) {
            getLogs().debug("Couldn't cleanup server folder!", e);
        }
    }

    @EventHandler
    public void onCloudDisconnect(CloudDisconnectedEvent event) {

        // if the daemon got disconnected from the cloud, stop every server
        // (sorry, but its better this way)
        closeEveryServer(null);
    }

}
