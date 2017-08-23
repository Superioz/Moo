package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.config.MooConfigType;
import de.superioz.moo.client.Moo;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;

@RunAsynchronous
public class MaintenanceCommand {

    private static final String LABEL = "maintenance";
    private static final String TOGGLE_COMMAND = "toggle";
    private static final String MOTD_COMMAND = "motd";

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        // subcommands
        completor.reactSubCommands(LABEL);
    }

    @Command(label = LABEL, usage = "[toggle]")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        // display the maintenance state
        boolean maintenance = (boolean) MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE);
        context.sendMessage(LanguageManager.get("maintenance-info", maintenance));
    }

    @Command(label = TOGGLE_COMMAND, parent = LABEL)
    public void onToggleCommand(BungeeCommandContext context, ParamSet args) {
        // toggles the maintenance state
        boolean maintenance = (boolean) MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE);

        context.sendMessage(LanguageManager.get("maintenance-toggle-load"));
        ResponseStatus status = Moo.getInstance().config(MooConfigType.MAINTENANCE, !maintenance + "");
        context.invalidArgument(status.isNok(), LanguageManager.get("maintenance-toggle-complete-failure", status));
        context.sendMessage(LanguageManager.get("maintenance-toggle-complete-success"));
    }

    @Command(label = MOTD_COMMAND, parent = LABEL, usage = "[newMotd]")
    public void onMotdCommand(BungeeCommandContext context, ParamSet args) {
        // if the player want to set the motd
        if(args.size() >= 1) {
            String newMotd = String.join(" ", args.getRange(0));

            // set maintenance motd
            context.sendMessage(LanguageManager.get("maintenance-motd-change-load"));
            ResponseStatus status = Moo.getInstance().config(MooConfigType.MAINTENANCE_MOTD, newMotd);
            context.invalidArgument(status.isNok(), LanguageManager.get("maintenance-motd-change-complete-failure", status));
            context.sendMessage(LanguageManager.get("maintenance-motd-change-complete-success"));
            return;
        }

        // otherwise display the motd
        String motd = (String) MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE_MOTD);
        context.sendMessage(LanguageManager.get("maintenance-motd-info", motd));
    }

}
