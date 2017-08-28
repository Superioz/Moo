package de.superioz.moo.daemon;

import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.client.Moo;
import de.superioz.moo.daemon.commands.MainCommand;
import de.superioz.moo.daemon.listeners.PacketPatternStateListener;
import de.superioz.moo.daemon.listeners.ServerPacketListener;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.packet.PacketAdapting;

import java.nio.file.Paths;

/**
 * Module for config, commands, listeners, ...
 */
public class DaemonModule extends Module {

    private Daemon daemon;

    public DaemonModule(Daemon daemon) {
        this.daemon = daemon;
    }

    @Override
    public String getName() {
        return "daemon";
    }

    @Override
    protected void onEnable() {
        // load the configuration
        daemon.getLogs().info("Load configuration ..");
        JsonConfig config = Moo.getInstance().loadConfig(Paths.get("configuration").toFile(), "config");
        daemon.config = config;

        // debug mode
        daemon.getLogs().setDebugMode(config.get("debug"));
        daemon.getLogs().info("Debug Mode: " + (daemon.getLogs().isDebugMode() ? "ON" : "off"));

        // register the commands (would be possible to do it with Moo.initialize directly, but it's not a MooPlugin, so ...)
        CommandRegistry.getInstance().registerCommands(new MainCommand());

        // register listeners
        EventExecutor.getInstance().register(daemon);
        PacketAdapting.getInstance().register(new ServerPacketListener(), new PacketPatternStateListener());

        // if the config is loaded connect to the cloud
        if(config.isLoaded()) {
            Moo.getInstance().connect(config.get("daemon-name"),
                    ClientType.DAEMON, config.get("cloud-ip"),
                    config.get("cloud-port"));
        }
    }

    @Override
    protected void onDisable() {

    }
}
