package de.superioz.moo.proxy.commands.perm;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.network.common.MooGroup;
import de.superioz.moo.network.common.MooPlayer;
import de.superioz.moo.network.common.MooProxy;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.network.queries.ResponseStatus;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.command.BungeeParamSet;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;

@RunAsynchronous
public class RankCommand {

    private static final String RANK_LABEL = "rank";
    private static final String UPRANK_LABEL = "uprank";
    private static final String DOWNRANK_LABEL = "downrank";

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.react(1, Collections.singletonList(
                LanguageManager.get("available-groups",
                        StringUtil.getListToString(MooQueries.getInstance().listGroups(), ", ", Group::getName))
        ), RANK_LABEL);
    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.react(1, StringUtil.getStringList(
                ProxyServer.getInstance().getPlayers(), ProxiedPlayer::getDisplayName)
        );

        completor.react(2, StringUtil.getStringList(
                MooQueries.getInstance().listGroups(), Group::getName
        ), RANK_LABEL);
    }

    @Command(label = RANK_LABEL, usage = "<player> [group]")
    public void onRankCommand(BungeeCommandContext context, BungeeParamSet args) {
        // get player and therefore his group
        String playerName = args.get(0);
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(player.nexists(), "error-player-doesnt-exist", playerName);
        MooGroup currentGroup = player.getGroup();

        // if he only typed the playername send rank information
        if(args.size() == 1) {
            // if the player uses the 'steps' flag he want to rank the player
            context.sendMessage("rank-of", playerName, currentGroup.getColor() + currentGroup.getName(), currentGroup.getName());
            return;
        }

        // rank the player to the given group
        String groupName = args.get(1);
        MooGroup newGroup = args.getMooGroup(groupName);
        context.invalidArgument(newGroup.nexists(), "group-doesnt-exist", groupName);

        // execute the ranking
        context.sendMessage("rank-player-load", playerName, groupName);
        ResponseStatus status = player.setGroup(newGroup);
        context.sendMessage("rank-player-complete", status);
    }

    @Command(label = UPRANK_LABEL, usage = "<player> [steps]")
    public void onUprankCommand(BungeeCommandContext context, BungeeParamSet args) {
        // get player and therefore his group
        String playerName = args.get(0);
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(player.nexists(), "error-player-doesnt-exist", playerName);

        // get the steps for getting the group
        int steps = args.getInt(1, 1, integer -> integer <= 0);
        MooGroup newGroup = MooProxy.getGroup(player.getGroup(), steps, steps > 0);
        context.invalidArgument(newGroup.nexists(), "group-doesnt-exist", newGroup.getName());

        // execute the ranking
        context.sendMessage("rank-player-load", playerName, newGroup.getName());
        ResponseStatus status = player.setGroup(newGroup);
        context.sendMessage("rank-player-complete", status);
    }

    @Command(label = DOWNRANK_LABEL, usage = "<player> [steps]")
    public void onDownrankCommand(BungeeCommandContext context, BungeeParamSet args) {
        // get player and therefore his group
        String playerName = args.get(0);
        MooPlayer player = args.getMooPlayer(playerName);
        context.invalidArgument(player.nexists(), "error-player-doesnt-exist", playerName);

        // get the steps for getting the group
        int steps = args.getInt(1, 1, integer -> integer <= 0);
        MooGroup newGroup = MooProxy.getGroup(player.getGroup(), steps, steps < 0);
        context.invalidArgument(newGroup.nexists(), "group-doesnt-exist", newGroup.getName());

        // execute the ranking
        context.sendMessage("rank-player-load", playerName, newGroup.getName());
        ResponseStatus status = player.setGroup(newGroup);
        context.sendMessage("rank-player-complete", status);
    }

}
