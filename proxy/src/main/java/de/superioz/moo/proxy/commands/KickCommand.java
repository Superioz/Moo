package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.protocol.common.ResponseStatus;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

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
        String playerName = args.get(0);
        context.invalidArgument(!Validation.PLAYERNAME.matches(playerName), LanguageManager.get("error-invalid-player-name"));

        // list kickor
        PlayerData executor = null;
        if(!context.isConsole()) {
            ProxiedPlayer player = (ProxiedPlayer) context.getCommandSender();
            executor = new PlayerData();
            executor.setUuid(player.getUniqueId());
            executor.setLastName(player.getName());
            executor.setGroup(MooQueries.getInstance().getGroup(player.getUniqueId()).getName());
        }

        // gets the reason for the kick
        String reason = "";
        if(args.size() > 1) {
            reason = String.join(" ", args.getRange(1));
        }

        // kicks the player
        context.sendMessage(LanguageManager.get("kick-player-load", playerName));
        ResponseStatus status = MooQueries.getInstance().kickPlayer(executor, playerName, LanguageManager.get("kick-player", reason));
        context.invalidArgument(status.isNok(), LanguageManager.get("kick-player-complete-failure", status));

        // send either directly or teamchat
        if(BungeeTeamChat.getInstance().canTeamchat(context.getCommandSender())) {
            String displayReason = reason.length() > 15 ? reason.substring(0, 15) : reason;

            // send message
            BungeeTeamChat.getInstance().send(LanguageManager.get("kick-player-teamchat",
                    playerName, BungeeTeamChat.getInstance().getColoredName(context.getCommandSender()),
                    displayReason, reason.equals(displayReason) ? "" : reason));
        }
        else {
            context.sendMessage(LanguageManager.get("kick-player-complete-success", playerName));
        }
    }

}
