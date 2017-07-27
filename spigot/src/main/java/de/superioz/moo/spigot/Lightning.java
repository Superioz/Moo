package de.superioz.moo.spigot;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Logs;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooPlugin;
import de.superioz.moo.client.common.MooPluginStartup;
import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.util.MooPluginUtil;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.spigot.listeners.ChatListener;
import de.superioz.moo.spigot.listeners.PacketRespondListener;
import de.superioz.moo.spigot.listeners.ServerListener;
import de.superioz.moo.spigot.task.ServerInfoTask;
import de.superioz.moo.spigot.util.LanguageManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

@Getter
public class Lightning extends JavaPlugin implements EventListener, MooPlugin {

    private static Lightning instance;

    public static Lightning getInstance() {
        if(instance == null) {
            instance = new Lightning();
        }
        return instance;
    }

    private Logs logs;
    private LanguageManager languageManager;
    private JsonConfig jsonConfig;

    private final ExecutorService executors = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("lightning-pool-%d").build());
    private ServerInfoTask serverInfoTask;

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
        if(jsonConfig.isLoaded()) {
            Moo.getInstance().connect(jsonConfig.get("server-name"), ClientType.SERVER, jsonConfig.get("cloud-ip"), jsonConfig.get("cloud-port"));
        }

        // start serverInfo task
        this.executors.execute(this.serverInfoTask = new ServerInfoTask(5 * 1000));
    }

    @Override
    public void onDisable() {
        //
    }

    @Override
    public void loadConfig() {
        jsonConfig = MooPluginUtil.loadConfig(getDataFolder(), "config");

        getLogger().info("Loading properties ..");
        languageManager = new LanguageManager("language.properties");
    }

    @Override
    public void loadPluginStartup(MooPluginStartup startup) {
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
    public void onStart(CloudConnectedEvent event) {
        getLogger().info("** AUTHENTICATION STATUS: " + (event.getStatus()) + " **");

        if(event.getStatus() == ResponseStatus.OK) {
            executors.execute(() -> ProxyCache.getInstance().loadGroups());
        }
    }

}
