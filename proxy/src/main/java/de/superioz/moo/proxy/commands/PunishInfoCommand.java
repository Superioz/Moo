package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.common.punishment.BanCategory;
import de.superioz.moo.api.common.punishment.BanType;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@RunAsynchronous
public class PunishInfoCommand {

    private static final String LABEL = "punishinfo";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {

    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName)
        );
    }

    @Command(label = LABEL, usage = "<player>")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        PlayerProfile playerInfo = args.get(0, PlayerProfile.class);
        context.invalidArgument(playerInfo == null, LanguageManager.get("player-doesnt-exist", args.get(0)));

        // list current ban/mute
        Ban currentBan = playerInfo.getCurrentBan();

        // if he is not banned and muted
        if(currentBan == null) {
            context.sendMessage(LanguageManager.get("punishment-player-is-nothing", playerInfo.getName()));
            return;
        }
        context.sendMessage(LanguageManager.get("punishment-header", playerInfo.getName()));

        // if the current ban is not null list the executor and send info
        // otherwise send (not-banned)
        if(currentBan != null) {
            String banExecutorName = CommandContext.CONSOLE_NAME;
            PlayerData banExecutor = MooQueries.getInstance().getPlayerData(currentBan.by);
            if(banExecutor != null) banExecutorName = banExecutor.lastName;

            // values for formatting
            long current = System.currentTimeMillis();
            String start = TimeUtil.getFormat(currentBan.start);
            BanCategory subType = currentBan.getSubType();
            String typeColor = subType.getBanType() == BanType.GLOBAL ? "&c" : "&9";
            String end = TimeUtil.getFormat(current + currentBan.duration);

            context.sendEventMessage(LanguageManager.get("punishment-ban-info",
                    start, typeColor + subType.getName(),
                    "Details",
                    start,
                    end,
                    typeColor + currentBan.reason,
                    BungeeTeamChat.getInstance().getColor(banExecutor == null ? null : banExecutor.uuid) + banExecutorName));
        }
        else {
            context.sendMessage(LanguageManager.get("punishment-player-isnt-banned", playerInfo.getName()));
        }
    }

}
