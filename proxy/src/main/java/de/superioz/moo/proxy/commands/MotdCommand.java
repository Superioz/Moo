package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.protocol.packets.PacketConfig;
import de.superioz.moo.proxy.command.BungeeCommandContext;

@RunAsynchronous
public class MotdCommand {

    private static final String LABEL = "motd";

    @Command(label = LABEL, usage = "[newMotd]")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        // if the player want to set the motd
        if(args.size() >= 1) {
            String newMotd = String.join(" ", args.getRange(0));

            // change motd
            context.sendMessage(LanguageManager.get("motd-change-load"));
            ResponseStatus status = Moo.getInstance().config(PacketConfig.Command.CHANGE, PacketConfig.Type.MOTD, newMotd);
            context.invalidArgument(status.isNok(), LanguageManager.get("motd-change-complete-failure", status));
            context.sendMessage(LanguageManager.get("motd-change-complete-success"));
            return;
        }

        // otherwise display the motd
        String motd = ProxyCache.getInstance().getConfigEntry(PacketConfig.Type.MOTD);
        context.sendMessage(LanguageManager.get("motd-info", motd));
    }

}
