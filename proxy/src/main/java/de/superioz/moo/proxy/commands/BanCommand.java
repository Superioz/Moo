package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.common.punishment.BanCategory;
import de.superioz.moo.api.common.punishment.BanSubType;
import de.superioz.moo.api.common.punishment.BanType;
import de.superioz.moo.api.common.punishment.PunishmentManager;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.network.common.MooQueries;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import javafx.util.Pair;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
                                ", ", BanSubType::getName))
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
    public void onBanCommand(BungeeCommandContext context, ParamSet args) {
        CommandSender sender = context.getCommandSender();
        String playerName = args.get(0);
        context.invalidArgument(!Validation.PLAYERNAME.matches(playerName), "error-invalid-player-name", playerName);
        context.invalidArgument(playerName.equalsIgnoreCase(sender.getName()), "ban-cannot-ban-yourself");

        // list the ban reason
        // if null = rip (or invalid ban reason)
        BanSubType banSubType = PunishmentManager.getInstance().getBanSubType(args.get(1));
        context.invalidArgument(banSubType == null || banSubType.getType() == BanType.CHAT, "ban-invalid-reason", args.get(1));

        // executes the ban
        executeBan(context, playerName, banSubType.getBanSubType(), banSubType.getName(), 0L);
    }

    @Command(label = TEMPBAN_LABEL, usage = "<player> <reason> <time>")
    public void onTempbanCommand(BungeeCommandContext context, ParamSet args) {
        CommandSender sender = context.getCommandSender();
        String playerName = args.get(0);
        context.invalidArgument(!Validation.PLAYERNAME.matches(playerName), "error-invalid-player-name", playerName);
        context.invalidArgument(playerName.equalsIgnoreCase(sender.getName()), "ban-cannot-ban-yourself");

        // list the ban reason
        // if null = rip (or invalid ban reason)
        BanSubType banSubType = PunishmentManager.getInstance().getBanSubType(args.get(1));
        context.invalidArgument(banSubType == null || banSubType.getType() == BanType.CHAT, "ban-invalid-reason", args.get(1));

        // list duration
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
        executeBan(context, playerName, banSubType.getBanSubType(), banSubType.getName(), duration);
    }

    /**
     * Executes the ban
     *
     * @param context    The command context
     * @param target     The ban target name
     * @param banSubType The bansubtype
     * @param reason     The banreason
     * @param duration   The duration (or null for automatic)
     */
    private void executeBan(BungeeCommandContext context, String target, BanCategory banSubType, String reason, Long duration) {
        context.sendMessage(LanguageManager.get("ban-load"));
        ResponseStatus status = MooQueries.getInstance().ban(
                context.isConsole() ? null : ((ProxiedPlayer) context.getCommandSender()).getUniqueId(),
                target, banSubType, reason, duration,
                LanguageManager.get("ban-message-temp"),
                LanguageManager.get("ban-message-perm")
        );
        context.invalidArgument(status == ResponseStatus.NOT_FOUND, LanguageManager.get("error-player-doesnt-exist", target));
        context.invalidArgument(status == ResponseStatus.FORBIDDEN, LanguageManager.get("ban-not-allowed-to", target));
        context.invalidArgument(status == ResponseStatus.CONFLICT, LanguageManager.get("ban-player-already-banned", target));

        // send teamchat message or only direct to him
        if(!BungeeTeamChat.getInstance().canTeamchat(context.getCommandSender())) {
            context.sendMessage(LanguageManager.get("ban-complete", status));
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
                target, executor, typeColor + banSubType.getName(),
                Arrays.asList("Details", start, end, typeColor + reason, executor))
        );
    }

}
