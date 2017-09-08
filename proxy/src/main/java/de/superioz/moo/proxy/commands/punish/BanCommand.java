package de.superioz.moo.proxy.commands.punish;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.common.punishment.BanReason;
import de.superioz.moo.api.common.punishment.BanType;
import de.superioz.moo.api.common.punishment.PunishmentManager;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.network.common.MooPlayer;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.command.BungeeParamSet;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import javafx.util.Pair;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunAsynchronous
public class BanCommand {

    private static final String BAN_LABEL = "ban";
    private static final String TEMPBAN_LABEL = "tempban";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.react(1, Collections.singletonList(
                LanguageManager.get("available-reasons",
                        StringUtil.getListToString(PunishmentManager.getInstance().getBanSubTypes()
                                        .stream().filter(banReason -> banReason.getType() == BanType.GLOBAL)
                                        .collect(Collectors.toList()),
                                ", ", BanReason::getName))
        ));

        // time unit
        helper.react(2, Arrays.asList(
                LanguageManager.get("time-syntax"),
                LanguageManager.get("available-units")
        ), TEMPBAN_LABEL);
    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName)
        );
    }

    @Command(label = BAN_LABEL, usage = "<player> <reason>")
    public void ban(BungeeCommandContext context, BungeeParamSet args) {
        CommandSender sender = context.getCommandSender();
        UUID executor = context.isConsole() ? null : ((ProxiedPlayer) context.getCommandSender()).getUniqueId();
        String playerName = args.get(0);

        // get player
        context.invalidArgument(playerName.equalsIgnoreCase(sender.getName()), "ban-cannot-ban-yourself");
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(!player.exists(), "error-player-doesnt-exist", playerName);

        // get the ban reason
        // if null = rip (or invalid ban reason)
        String reason = args.get(1);
        BanReason banReason = PunishmentManager.getInstance().getBanSubType(reason);
        context.invalidArgument(banReason == null || banReason.getType() == BanType.CHAT, "ban-invalid-reason", reason);

        // executes the ban
        context.sendMessage("ban-load");
        reactStatus(context, player.ban(executor, banReason), playerName, banReason, 0L);
    }

    @Command(label = TEMPBAN_LABEL, usage = "<player> <reason> <time>")
    public void tempban(BungeeCommandContext context, BungeeParamSet args) {
        CommandSender sender = context.getCommandSender();
        UUID executor = context.isConsole() ? null : ((ProxiedPlayer) context.getCommandSender()).getUniqueId();
        String playerName = args.get(0);

        // get player
        context.invalidArgument(playerName.equalsIgnoreCase(sender.getName()), "ban-cannot-ban-yourself");
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(!player.exists(), "error-player-doesnt-exist", playerName);

        // get the ban reason
        // if null = rip (or invalid ban reason)
        String reason = args.get(1);
        BanReason banReason = PunishmentManager.getInstance().getBanSubType(reason);
        context.invalidArgument(banReason == null || banReason.getType() == BanType.CHAT, "ban-invalid-reason", reason);

        // get duration
        List<String> rawDurations = args.getRange(2);
        long duration = 0L;
        for(String rawDuration : rawDurations) {
            if(rawDuration.equals("-1")) {
                duration = -1;
                break;
            }
            Pair<Integer, TimeUnit> pair = TimeUtil.getTime(rawDuration);
            duration += pair.getValue().toMillis(pair.getKey());
        }

        // executes the ban
        context.sendMessage("ban-load");
        reactStatus(context, player.ban(executor, banReason), playerName, banReason, duration);
    }

    private void reactStatus(BungeeCommandContext context, ResponseStatus status,
                             String playerName, BanReason reason, long duration) {
        context.invalidArgument(status == ResponseStatus.FORBIDDEN, "ban-not-allowed-to", playerName);
        context.invalidArgument(status == ResponseStatus.CONFLICT, "ban-player-already-banned", playerName);

        // send teamchat message or only direct to him
        if(!BungeeTeamChat.getInstance().canTeamchat(context.getCommandSender())) {
            context.sendMessage("ban-complete", status);
            return;
        }

        // get replacements
        long current = System.currentTimeMillis();
        String executor = BungeeTeamChat.getInstance().getColoredName(context.getCommandSender());
        String typeColor = "&c";
        String start = TimeUtil.getFormat(current);
        String end = TimeUtil.getFormat(current + duration);

        // send the chat
        BungeeTeamChat.getInstance().send(LanguageManager.get("ban-teamchat-announcement",
                playerName, executor, typeColor + reason.getName(),
                Arrays.asList("Details", start, end, typeColor + reason, executor))
        );
    }

}
