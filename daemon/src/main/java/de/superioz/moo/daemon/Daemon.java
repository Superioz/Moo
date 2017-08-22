package de.superioz.moo.daemon;

import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.console.CommandTerminal;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Loogger;
import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.api.utils.SystemUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.events.CloudDisconnectedEvent;
import de.superioz.moo.daemon.commands.MainCommand;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.listeners.ServerPacketListener;
import de.superioz.moo.daemon.task.ServerStartTask;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.packet.PacketAdapting;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Getter
public class Daemon implements EventListener {

    public static final Pattern PREDEFINED_SERVER_PATTERN = Pattern.compile("\\w+(:\\d+)?");

    private static Daemon instance;

    public static Daemon getInstance() {
        if(instance == null) {
            instance = new Daemon();
        }
        return instance;
    }

    public DaemonInstance server;
    @Setter(value = AccessLevel.PRIVATE)
    public JsonConfig config;
    @Setter(value = AccessLevel.PRIVATE)
    public Loogger logs;

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
     * @param autoSave    If the server should auto save or just get deleted after shutdown
     * @param amount      The amount of this type of server to start
     * @param resultOfReq The result of the request (Server started or rip)
     */
    public void startServer(String type, boolean autoSave, int amount, Consumer<Server> resultOfReq) {
        for(int i = 0; i < amount; i++) {
            ServerStartTask task = new ServerStartTask(type, -1, autoSave, resultOfReq);
            Daemon.getInstance().getServer().getServerQueue().getQueue().offer(task);
        }
    }

    /**
     * The main method of the daemon
     *
     * @param args .
     */
    public static void main(String[] args) {
        MooLogger logger = new MooLogger("Daemon");
        getInstance().setLogs(new Loogger(logger));
        getInstance().getLogs().enableFileLogging().prepareNativeStreams();

        getInstance().getLogs().warning("*** STOPPING THE DAEMON FORCEFULLY RESULTS IN GHOST PROCESSES  ***");
        getInstance().getLogs().warning("*** IT IS RECOMMENDED TO USE THE 'STOP' COMMAND BEFORE CLOSING ***");
        getInstance().getLogs().info("Start daemon @" + System.getProperty("user.dir") + " (" + (SystemUtil.isWindows() ? "WINDOWS" : "LINUX") + ")");
        Moo.initialize(logger);

        // load the configuration
        Daemon.getInstance().getLogs().info("Load configuration ..");
        JsonConfig config = Moo.getInstance().loadConfig(Paths.get("configuration").toFile(), "config");
        getInstance().setConfig(config);

        Daemon.getInstance().getLogs().setDebugMode(config.get("debug"));
        logger.info("Debug Mode: " + (Daemon.getInstance().getLogs().isDebugMode() ? "ON" : "off"));

        // register the commands (would be possible to do it with Moo.initialize directly, but it's not a MooPlugin, so ...)
        CommandRegistry.getInstance().registerCommands(new MainCommand());

        // register listeners
        EventExecutor.getInstance().register(getInstance());
        PacketAdapting.getInstance().register(new ServerPacketListener());

        // if the config is loaded connect to the cloud
        if(config.isLoaded()) {
            Moo.getInstance().connect(config.get("daemon-name"), ClientType.DAEMON, config.get("cloud-ip"), config.get("cloud-port"));
        }

        // start the command terminal for fancy console input
        new CommandTerminal().start(true, getInstance().getLogs(), logger.getReader());
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
