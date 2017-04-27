package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.cloud.Cloud;

public class DaemonCommand {

    @Command(label = "requestserver")
    public void requestserver(CommandContext context, ParamSet set){
        Cloud.getLogger().info("Requesting a server to start ..");
        Cloud.getInstance().getMooProxy().requestServer("lobby", false, 1, System.out::println);
    }

}
