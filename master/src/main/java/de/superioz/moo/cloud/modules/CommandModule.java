package de.superioz.moo.cloud.modules;

import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.commands.*;

public class CommandModule extends Module {

    @Override
    public String getName() {
        return "command";
    }

    @Override
    protected void onEnable() {
        Cloud.getInstance().getLogger().debug("Registering commands ..");
        EventExecutor.getInstance().register(new CloudCommand());
        CommandRegistry.getInstance().registerCommandsSeperately(new CloudCommand(),
                new DatabaseCommand(),
                new CacheCommand(), new PatternCommand(),
                new DaemonCommand());
        Cloud.getInstance().getLogger().debug("Commands registered.");
    }

    @Override
    protected void onDisable() {
        CommandRegistry.getInstance().unregisterAll();
    }

}
