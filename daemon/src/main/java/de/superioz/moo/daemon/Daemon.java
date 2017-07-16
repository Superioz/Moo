package de.superioz.moo.daemon;

import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.console.CommandTerminal;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Logs;
import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.api.utils.SystemUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.events.ClientConnectedEvent;
import de.superioz.moo.client.util.MooPluginUtil;
import de.superioz.moo.daemon.commands.MainCommand;
import de.superioz.moo.daemon.listeners.ServerPacketListener;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.packet.PacketAdapting;

import java.io.File;
import java.nio.file.Paths;

public class Daemon implements EventListener {

    private static Daemon instance;

    public static Daemon getInstance() {
        if(instance == null) {
            instance = new Daemon();
        }
        return instance;
    }

    public static DaemonInstance server;
    public static JsonConfig config;
    public static Logs logs;

    /**
     * The main method of the daemon
     *
     * @param args .
     */
    public static void main(String[] args) {
        MooLogger logger = new MooLogger("Daemon");
        logs = new Logs(logger).enableFileLogging();
        logs.prepareNativeStreams();

        Daemon.logs.info("Start daemon @" + System.getProperty("user.dir") + " (" + (SystemUtil.isWindows() ? "WINDOWS" : "LINUX") + ")");
        Moo.initialise(logger);

        // load the configuration
        Daemon.logs.info("Load configuration ..");
        config = MooPluginUtil.loadConfig(Paths.get("configuration"), "config");

        // register the commands (would be possible to do it with Moo.initialise directly, but it's not a MooPlugin, so ...)
        CommandRegistry.getInstance().registerCommands(new MainCommand());
        EventExecutor.getInstance().register(getInstance());

        // if the config is loaded connect to the cloud
        if(config.isLoaded()) {
            Moo.getInstance().connect(config.get("daemon-name"), ClientType.DAEMON, config.get("cloud-ip"), config.get("cloud-port"));
        }

        // start the command terminal for fancy console input
        new CommandTerminal().start(true, logs, logger.getReader());

        //
        PacketAdapting.getInstance().register(new ServerPacketListener());
    }

    @EventHandler
    public void onStart(ClientConnectedEvent event) {
        logs.info("** AUTHENTICATION STATUS: " + (event.getStatus()) + " **");

        // create daemon instance
        server = new DaemonInstance(new File((String) config.get("patterns-folder")),
                new File((String) config.get("servers-folder")), config.get("start-file")).fetchPatterns();
        server.createFolders();
    }

}
