package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.network.common.MooPlayer;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.network.server.MooProxy;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@RunAsynchronous
public class KickCommand {

    private static final String LABEL = "kick";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {

    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName)
        );
    }

    @Command(label = LABEL, usage = "<player> [reason]")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        CommandSender sender = context.getCommandSender();
        UUID executor = context.isConsole() ? null : ((ProxiedPlayer) context.getCommandSender()).getUniqueId();
        String playerName = args.get(0);

        // get player
        MooPlayer player = MooProxy.getInstance().getPlayer(playerName);
        context.invalidArgument(playerName.equalsIgnoreCase(sender.getName()), "kick-cannot-kick-yourself");
        context.invalidArgument(!player.isOnline(), "error-player-doesnt-exist", playerName);

        // gets the reason for the kick
        String reason = args.size() > 1 ? String.join(" ", args.getRange(1)) : "";

        // kicks the player
        context.sendMessage("kick-player-load", playerName);
        ResponseStatus status = player.kickPlayer(executor, LanguageManager.get("kick-player", reason));
        context.invalidArgument(status.isNok(), "kick-player-complete-failure", status);

        // send either directly or teamchat
        if(BungeeTeamChat.getInstance().canTeamchat(context.getCommandSender())) {
            String displayReason = StringUtil.cutOff(reason, 15);

            // send message
            BungeeTeamChat.getInstance().send(LanguageManager.get("kick-player-teamchat",
                    playerName, BungeeTeamChat.getInstance().getColoredName(context.getCommandSender()),
                    displayReason, reason.equals(displayReason) ? "" : reason));
        }
        else {
            context.sendMessage("kick-player-complete-success", playerName);
        }
    }

}
