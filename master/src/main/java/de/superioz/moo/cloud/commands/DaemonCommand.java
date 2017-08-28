package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.cloud.Cloud;

public class DaemonCommand {

    @Command(label = "reqserver", usage = "<type> [amount]")
    public void requestserver(CommandContext context, ParamSet set) {
        String type = set.get(0);
        int amount = set.getInt(1, 1);

        // normally we would check if the amount of servers is too high and blabla, but
        // it's the cloud, so FULL access
        context.sendMessage("Requesting server to start .. (" + amount + "x " + type + ")");
        Cloud.getInstance().getMooProxy().requestServer(type, false, amount, packet -> context.sendMessage(packet.toString()));
    }

    @Command(label = "reqshutdown", usage = "<host> <port>")
    public void requestshutdown(CommandContext context, ParamSet args) {
        String host = args.get(0);
        int port = args.getInt(1, -1);

        // check the values
        if(host.isEmpty() || port < 0) {
            context.sendMessage(ConsoleColor.RED + "Host or port invalid! (Host: " + host + "; Port: " + port + ")");
            return;
        }

        context.sendMessage("Requesting a server to shutdown .. (" + host + ":" + port + ")");
        Cloud.getInstance().getMooProxy().requestServerShutdown(host, port, packet -> context.sendMessage(packet.toString()));
    }

}
