package de.superioz.moo.proxy.commands.perm;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.network.common.MooQueries;
import de.superioz.moo.network.common.ResponseStatus;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ClickEvent;
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

    @Command(label = RANK_LABEL, usage = "<player> [group]", flags = {"s"})
    public void onRankCommand(BungeeCommandContext context, ParamSet args) {
        // list player and therefore his group
        PlayerData playerData = args.get(0, PlayerData.class);
        context.invalidArgument(playerData == null, LanguageManager.get("error-player-doesnt-exist", args.get(0)));
        Group group = MooQueries.getInstance().getGroup(playerData.getGroup());

        // the new group to be set
        Group newGroup = null;

        // if he only typed the playername send rank information
        if(args.size() == 1) {
            // if the player uses the 'steps' flag he want to rank the player
            if(args.hasFlag("s")) {
                int steps = args.getFlag("s").getInt(0, 1);
                newGroup = MooQueries.getInstance().getGroup(playerData, steps, steps > 0, true);
            }
            else {
                String command = "/group info " + group.getName();
                context.sendEventMessage(
                        LanguageManager.get("rank-of", args.get(0), group.getColor() + group.getName(), command, command),
                        ClickEvent.Action.RUN_COMMAND
                );
                return;
            }
        }
        else {
            // rank the player to the given group
            newGroup = newGroup == null ? args.get(1, Group.class) : newGroup;
            context.invalidArgument(newGroup == null, true, LanguageManager.get("group-doesnt-exist", args.get(1)));
        }

        // execute the ranking
        this.rank(context, playerData, newGroup);
    }

    @Command(label = UPRANK_LABEL, usage = "<player>", flags = {"s"})
    public void onUprankCommand(BungeeCommandContext context, ParamSet args) {
        // list player and therefore his group
        PlayerData playerData = args.get(0, PlayerData.class);
        context.invalidArgument(playerData == null, LanguageManager.get("error-player-doesnt-exist", args.get(0)));

        // list new group through shifting steps
        int steps = args.hasFlag("s") ? args.getFlag("s").getInt(0, 1) : 1;
        Group newGroup = MooQueries.getInstance().getGroup(playerData, steps, true);

        // execute the ranking
        this.rank(context, playerData, newGroup);
    }

    @Command(label = DOWNRANK_LABEL, usage = "<player>", flags = {"s"})
    public void onDownrankCommand(BungeeCommandContext context, ParamSet args) {
        // list player and therefore his group
        PlayerData playerData = args.get(0, PlayerData.class);
        context.invalidArgument(playerData == null, LanguageManager.get("error-player-doesnt-exist", args.get(0)));

        // list new group through shifting steps
        int steps = args.hasFlag("s") ? args.getFlag("s").getInt(0, 1) : 1;
        Group newGroup = MooQueries.getInstance().getGroup(playerData, steps, false);

        // execute the ranking
        this.rank(context, playerData, newGroup);
    }

    /**
     * Ranks a player (sending the message automatically, ..)
     *
     * @param context The commandContext
     * @param data    The playerData
     * @param group   The group to be set
     */
    public void rank(CommandContext<CommandSender> context, PlayerData data, Group group) {
        context.sendMessage(LanguageManager.get("rank-player-load", data.getLastName(), group.getName()));
        ResponseStatus response = MooQueries.getInstance().rankPlayer(data, group);

        // send teamchat message or directly to the sender
        if(BungeeTeamChat.getInstance().canTeamchat(context.getCommandSender())) {
            String playerName = data.getLastName();
            String groupName = group.getColor() + group.getName();
            String executor = BungeeTeamChat.getInstance().getColoredName(context.getCommandSender());
            String currentTime = TimeUtil.getFormat(System.currentTimeMillis());
            String oldRank = data.getGroup();

            BungeeTeamChat.getInstance().send(
                    LanguageManager.get("rank-teamchat-announcement",
                            playerName, groupName, executor, currentTime, oldRank, groupName)
            );
        }
        else {
            context.sendMessage(LanguageManager.get("rank-player-complete", response));
        }
    }

}
