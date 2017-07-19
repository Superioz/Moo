package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.cloud.Cloud;

public class DaemonCommand {

    @Command(label = "requestserver", usage = "<type> [amount]")
    public void requestserver(CommandContext context, ParamSet set) {
        String type = set.get(0);
        int amount = set.getInt(1, 1);

        // check if a bungeecord server is connected
        // if not, error!
        if(Cloud.getInstance().getHub().getProxyClients().isEmpty()) {
            context.sendMessage(ConsoleColor.RED + "Can't start server without a bungeecord connected!");
            return;
        }

        context.sendMessage("Requesting server to start .. (" + amount + "x " + type + ")");
        Cloud.getInstance().getMooProxy().requestServer(type, false, amount, packet -> context.sendMessage(packet.toString()));
    }

    @Command(label = "requestshutdown", usage = "<host> <port>")
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
