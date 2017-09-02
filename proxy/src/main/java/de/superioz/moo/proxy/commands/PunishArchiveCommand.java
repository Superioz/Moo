package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.common.punishment.BanType;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.DisplayFormats;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.network.common.MooQueries;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunAsynchronous
public class PunishArchiveCommand {

    private static final String LABEL = "punisharchive";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {

    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName)
        );
    }

    @Command(label = LABEL, usage = "<player> [page]", flags = "l")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        String playerName = args.get(0);
        context.invalidArgument(!Validation.PLAYERNAME.matches(playerName), LanguageManager.get("error-invalid-player-name", playerName));

        // if live fetching is enabled don't cache the list
        boolean liveFetching = args.hasFlag("l");

        // list the ban archive of the given player
        // if not banned display error message
        List<Ban> banArchiveList = null;
        if(!liveFetching) {
            banArchiveList = context.get(playerName);
        }
        if(banArchiveList == null) {
            banArchiveList = MooQueries.getInstance().getBanArchive(playerName);

            if(!liveFetching) {
                context.setExpireAfterCreation(playerName, banArchiveList, 30, TimeUnit.SECONDS);
            }
        }
        context.invalidArgument(banArchiveList.isEmpty(), LanguageManager.get("punishment-archive-list-empty"));

        // display archive at given page
        PageableList<Ban> pageableList = new PageableList<>(banArchiveList);

        // list page
        int page = args.getInt(0, 0);

        // sends the list
        String entryFormat = LanguageManager.get("punishment-archive-list-entry");
        DisplayFormats.sendPageableList(context, pageableList, page,
                LanguageManager.get("punishment-archive-list-empty"),
                LanguageManager.get("punishment-archive-list-header"), LanguageManager.get("punishment-archive-list-entry-empty"),
                ban -> {
                    String banExecutorName = CommandContext.CONSOLE_NAME;
                    PlayerData banExecutor = MooQueries.getInstance().getPlayerData(ban.getBy());
                    if(banExecutor != null) banExecutorName = banExecutor.getLastName();

                    String typeColor = ban.getSubType().getBanType() == BanType.GLOBAL ? "&c" : "&9";
                    String start = TimeUtil.getFormat(ban.getStart());
                    String end = TimeUtil.getFormat(ban.until());

                    context.sendEventMessage(
                            LanguageManager.format(entryFormat,
                                    start, end, typeColor + ban.getReason(),
                                    Arrays.asList("Details", start, end, typeColor + ban.getReason(),
                                            BungeeTeamChat.getInstance().getColor(banExecutor == null ? null : banExecutor.getUuid()) + banExecutorName)),
                            ClickEvent.Action.RUN_COMMAND
                    );
                }, () -> {
                    String command = "/punisharchive " + playerName + " " + (page + 1);
                    context.sendEventMessage(
                            LanguageManager.get("punishment-archive-next-page", command, command),
                            ClickEvent.Action.RUN_COMMAND
                    );
                });
    }

}
