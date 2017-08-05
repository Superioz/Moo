package de.superioz.moo.cloud.modules;

import lombok.Getter;
import de.superioz.moo.api.common.punishment.Punishmental;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.cloud.Cloud;

import java.nio.file.Paths;

@Getter
public class ConfigModule extends Module {

    private Cloud cloud;
    private JsonConfig config;

    public ConfigModule(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public String getName() {
        return "config";
    }

    @Override
    protected void onEnable() {
        Cloud.getInstance().getLogger().info("Load configuration ..");
        config = new JsonConfig("config", Paths.get("configuration"));
        config.load(true, true);
        Cloud.getInstance().getLogger().info(config.isLoaded() ? "Loaded config @(" + config.getPath() + ")" : "Could not load config.");

        // debug
        Cloud.getInstance().getLogger().setDebugMode(config.get("debug", true));
        Cloud.getInstance().getLogger().info("Debug Mode is " + (Cloud.getInstance().getLogger().isDebugMode() ? "on" : "OFF"));

        config.getCache().keySet().forEach(s -> {
            String[] s0 = s.split(":");
            String key = s0[s0.length - 1].replace("$.", "");
            Object val = config.get(key);

            if(val != null) {
                Cloud.getInstance().getLogger().debug("-" + key + " has value '" + (val.toString().replace("\n", "\\n")) + "'");
            }
        });
        Punishmental.getInstance().init(
                config.get("minecraft.punishment-subtypes") + "",
                config.get("minecraft.punishment-reasons") + "");
    }

    @Override
    protected void onDisable() {
        config.getCache().clear();
    }
}
