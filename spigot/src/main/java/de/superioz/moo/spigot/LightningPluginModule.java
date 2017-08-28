package de.superioz.moo.spigot;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.netty.client.ClientType;
import de.superioz.moo.spigot.listeners.ChatListener;
import de.superioz.moo.spigot.listeners.ServerListener;
import de.superioz.moo.spigot.task.ServerInfoTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

@Getter
public class LightningPluginModule extends Module implements EventListener {

    private JsonConfig config;
    private LanguageManager languageManager;

    private ServerInfoTask serverInfoTask;

    @Override
    public String getName() {
        return "lightning";
    }

    @Override
    protected void onEnable() {
        EventExecutor.getInstance().register(this);

        // load config
        this.config = Moo.getInstance().loadConfig(Lightning.getInstance().getDataFolder());
        this.languageManager = new LanguageManager(Lightning.getInstance().getDataFolder(),
                s -> ChatColor.translateAlternateColorCodes('&', s));
        this.languageManager.load("language");

        // if disabled stop enabling
        if(!((boolean) config.get("activated"))) {
            Lightning.getInstance().getLogs().info("*** Lightning disabled. ***");
            return;
        }

        // register handler
        Moo.getInstance().registerHandler(o -> {
            if(o instanceof Listener) Bukkit.getServer().getPluginManager().registerEvents((Listener) o, Lightning.getInstance());
        }, new ChatListener(), new ServerListener());

        // connect to cloud
        if(config.isLoaded()) {
            Moo.getInstance().connect(config.get("group-id"), ClientType.SERVER, config.get("cloud-ip"), config.get("cloud-port"));
        }
    }

    @Override
    protected void onDisable() {
        // we do not need to send information that we are down, either the Daemon/Cloud does that for us, or the server just timeouts
    }

    @EventHandler
    public void onStart(CloudConnectedEvent event) {
        Lightning.getInstance().getLogs().info("** AUTHENTICATION STATUS: " + (event.getStatus().getColored()) + " **");

        // start serverInfo task
        Lightning.getInstance().getExecutors().execute(this.serverInfoTask = new ServerInfoTask(5 * 1000));
    }

}
