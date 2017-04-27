package de.superioz.moo.spigot;

import de.superioz.moo.spigot.listeners.ChatListener;
import de.superioz.moo.spigot.listeners.PacketRespondListener;
import de.superioz.moo.spigot.util.LanguageManager;
import lombok.Getter;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Logs;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooPlugin;
import de.superioz.moo.client.common.MooPluginStartup;
import de.superioz.moo.client.events.ClientConnectedEvent;
import de.superioz.moo.client.util.MooPluginUtil;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.spigot.listeners.ServerListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Function;

public class Lightning extends JavaPlugin implements EventListener, MooPlugin {

    @Getter
    private static Lightning instance;
    @Getter
    private static Logs logs;

    @Getter
    private static LanguageManager languageManager;
    private static JsonConfig config;

    @Override
    public void onEnable() {
        instance = this;
        Moo.initialise(this, getLogger());

        // logging
        logs = new Logs(getLogger());
        logs.prepareNativeStreams().enableFileLogging();

        // .
        this.loadConfig();

        // .
        EventExecutor.getInstance().register(this);
        if(config.isLoaded()) {
            Moo.getInstance().connect(config.get("server-name"), ClientType.SERVER, config.get("cloud-ip"), config.get("cloud-port"));
        }
    }

    @Override
    public void onDisable() {
        //
    }

    @Override
    public void loadConfig() {
        config = MooPluginUtil.loadConfig(getDataFolder(), "config");

        getLogger().info("Loading properties ..");
        languageManager = new LanguageManager("language.properties");
    }

    @Override
    public void loadPluginStartup(MooPluginStartup startup){
        startup.registerListeners(new ChatListener(), new PacketRespondListener(), new ServerListener());
    }

    @Override
    public Function<Object, Boolean> registerLeftOvers() {
        return object -> {
            if(object instanceof Listener) {
                if(MooPluginUtil.hasMooDependency(object)) {
                    Bukkit.getPluginManager().registerEvents((Listener) object, Lightning.this);
                    return true;
                }
            }
            return false;
        };
    }

    @EventHandler
    public void onStart(ClientConnectedEvent event) {
        getLogger().info("** AUTHENTICATION STATUS: " + (event.getStatus()) + " **");

        /*if(respond.status == ResponseStatus.OK) {
            ProxyCache.getInstance().loadGroups();
        }*/
    }

    // .
    public static JsonConfig getCustomConfig() {
        return config;
    }
}
