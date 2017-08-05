package de.superioz.moo.spigot;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.logging.Loogger;
import de.superioz.moo.api.module.ModuleRegistry;
import de.superioz.moo.client.Moo;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
public class Lightning extends JavaPlugin implements EventListener {

    public static Lightning getInstance() {
        if(instance == null) {
            instance = new Lightning();
        }
        return instance;
    }

    private static Lightning instance;
    private Loogger logs;

    private ModuleRegistry moduleRegistry;
    private LightningPluginModule pluginModule;

    private final ExecutorService executors = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("lightning-pool-%d").build());

    @Override
    public void onEnable() {
        instance = this;

        // initialises moo and plugin module
        Moo.initialise((logs = new Loogger(getLogger())).getLogger());
        this.pluginModule = new LightningPluginModule();
        this.moduleRegistry = new ModuleRegistry(getLogs());
        this.moduleRegistry.register(pluginModule);
    }

    @Override
    public void onDisable() {
        logs.disable();
        moduleRegistry.disableAll();
        Moo.getInstance().disconnect();
    }


}
