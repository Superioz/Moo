package de.superioz.moo.cloud.modules;

import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.cloud.commands.CacheCommand;
import de.superioz.moo.cloud.commands.CloudCommand;
import de.superioz.moo.cloud.commands.DaemonCommand;
import de.superioz.moo.cloud.commands.DatabaseCommand;

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
                new CacheCommand(),
                new DaemonCommand());
        Cloud.getInstance().getLogger().debug("Commands registered.");
    }

    @Override
    protected void onDisable() {
        CommandRegistry.getInstance().unregisterAll();
    }

}
