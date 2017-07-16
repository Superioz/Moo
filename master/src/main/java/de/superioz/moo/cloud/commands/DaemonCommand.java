package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.cloud.Cloud;

public class DaemonCommand {

    @Command(label = "requestserver", usage = "<type> [amount]")
    public void requestserver(CommandContext context, ParamSet set) {
        String type = set.get(0);
        int amount = set.getInt(0, 1);

        Cloud.getLogger().info("Requesting a server to start .. (" + amount + "x " + type + ")");
        Cloud.getInstance().getMooProxy().requestServer(type, false, amount, System.out::println);
    }

}
