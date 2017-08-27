package de.superioz.moo.spigot;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.CustomFile;
import de.superioz.moo.api.logging.ExtendedLogger;
import de.superioz.moo.api.module.ModuleRegistry;
import de.superioz.moo.api.modules.RedisModule;
import de.superioz.moo.client.Moo;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Paths;
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
    private ExtendedLogger logs;

    private ModuleRegistry moduleRegistry;
    private LightningPluginModule pluginModule;

    private final ExecutorService executors = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("lightning-pool-%d").build());

    @Override
    public void onEnable() {
        instance = this;

        // initialises moo and plugin module
        Moo.initialize((logs = new ExtendedLogger(getLogger())).getBaseLogger());
        this.pluginModule = new LightningPluginModule();
        this.moduleRegistry = new ModuleRegistry(getLogs());
        this.moduleRegistry.register(pluginModule);
        this.pluginModule.waitFor(module -> {
            if(module.getErrorReason() != null) return;
            CustomFile customFile = new CustomFile(((LightningPluginModule)module).getConfig().get("redis-config"), Paths.get("configuration"));
            customFile.load(true, true);
            moduleRegistry.register(new RedisModule(customFile.getFile(), getLogger()));
        });

        //
        Moo.getInstance().setSubPort(Bukkit.getPort());

        // list config
        logs.setDebugMode(pluginModule.getConfig().get("debug"));
        logs.info("Debug Mode is " + (logs.isDebugMode() ? "ON" : "off"));

        // summary
        moduleRegistry.sendModuleSummaryAsync();
    }

    @Override
    public void onDisable() {
        logs.disable();
        moduleRegistry.disableAll();
        Moo.getInstance().disconnect();
    }


}
