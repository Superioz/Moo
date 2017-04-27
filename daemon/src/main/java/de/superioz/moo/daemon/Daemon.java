package de.superioz.moo.daemon;

import jline.console.ConsoleReader;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.console.CommandTerminal;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Logs;
import de.superioz.moo.api.logging.MooLogger;
import de.superioz.moo.api.utils.SystemUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.util.MooPluginUtil;
import de.superioz.moo.daemon.commands.MainCommand;
import de.superioz.moo.daemon.listeners.ServerPacketListener;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.packet.PacketAdapting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Daemon {

    public static DaemonInstance server;
    public static JsonConfig config;
    public static Logs logs;

    public static void main(String[] args) throws IOException {
        logs = new Logs(new MooLogger("Daemon"));
        logs.prepareNativeStreams();

        Daemon.logs.info("Start daemon @" + System.getProperty("user.dir") + " (" + (SystemUtil.isWindows() ? "WINDOWS" : "LINUX") + ")");
        Moo.initialise(Logger.getLogger("DAEMON"));

        // config
        Daemon.logs.info("Load configuration ..");
        config = MooPluginUtil.loadConfig(Paths.get("configuration"), "config");

        if(config.isLoaded()) {
            Daemon.logs.info("Initialising cloud-connection ..");
            Moo.getInstance().connect(config.get("daemon-name"), ClientType.DAEMON, config.get("cloud-ip"), config.get("cloud-port"));
        }

        //
        CommandRegistry.getInstance().registerCommands(new MainCommand());
        Daemon.logs.info(new ArrayList<>(CommandRegistry.getInstance().getCommands()).toString());

        new CommandTerminal().start(true, logs, new ConsoleReader());

        //
        PacketAdapting.getInstance().register(new ServerPacketListener());
        //Daemon.logs.info(new ArrayList<>(PacketAdapting.getInstance().getHandlers()).toString());

        //
        server = new DaemonInstance(new File((String)config.get("patterns-folder")),
                new File((String)config.get("servers-folder")), config.get("start-file")).fetchPatterns();
        server.createFolders();
    }

}
