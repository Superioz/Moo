package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.client.Moo;
import de.superioz.moo.proxy.command.BungeeCommandContext;

public class ThunderCommand {

    @RunAsynchronous
    @Command(label = "thunder",
            permission = "network.moo",
            usage = "[connect:disconnect]")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        boolean activated = Moo.getInstance().isActivated();
        boolean connected = Moo.getInstance().isConnected();

        if(!activated) {
            context.sendMessage("&4The connection to the cloud has been deactivated in the config. " +
                    "Please enable it before using this command.");
            return;
        }

        if(args.size() == 0){
            context.sendMessage("&7Connection status: " + (connected ? "&aONLINE" : "&8Offline"));
            if(connected) {
                context.sendMessage("&7Cloud-Communication-System &bMoo &7v&f"
                        + Moo.getInstance().getClient().getMasterVersion()
                        + " &7(Ping: " + Moo.getInstance().ping() + "ms)");
            }
            return;
        }

        String arg = args.get(0);
        if(arg.equalsIgnoreCase("connect")){
            if(connected){
                context.sendMessage("&7This instance is already connected to the cloud.");
                return;
            }
            context.sendMessage("&7Connecting to the cloud ..");
            Moo.getInstance().reconnect();
        }
        else if(arg.equalsIgnoreCase("disconnect")){
            if(!connected){
                context.sendMessage("&7This instance is not connected to the cloud.");
            }
            else{
                context.sendMessage("&7Disconnecting from the cloud ..");
                Moo.getInstance().disconnect();
            }
        }
        else{
            context.sendCurrentUsage();
        }
    }
}
