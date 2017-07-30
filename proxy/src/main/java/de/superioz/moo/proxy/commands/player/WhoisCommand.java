package de.superioz.moo.proxy.commands.player;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.PlayerInfo;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.object.Ban;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.DisplayFormats;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RunAsynchronous
public class WhoisCommand {

    private static final String LABEL = "whois";

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
        PlayerInfo playerInfo = args.get(0, PlayerInfo.class);
        context.invalidArgument(playerInfo == null, LanguageManager.get("player-doesnt-exist", args.get(0)));

        // get current informations
        Ban currentBan = playerInfo.getCurrentBan();
        Ban currentChatBan = playerInfo.getCurrentChatBan();
        PlayerData data = playerInfo.getData();

        // get rough information about the player
        String playerName = args.get(0);
        UUID uuid = data.uuid;
        String ip = data.lastip;
        String rank = data.group;
        long coins = data.coins;
        int banPoints = data.banPoints;

        // get online status
        String firstOnline = TimeUtil.getFormat(data.firstOnline);
        String totalOnline = data.totalOnline == null ? "0" : TimeUnit.MILLISECONDS.toHours(data.totalOnline) + "h";
        String currentServer = data.currentServer;
        int currentProxy = data.currentProxy;
        long onlineSince = TimeUnit.MILLISECONDS.toMinutes(data.getCurrentOnline());
        String offlineSince = TimeUtil.getFormat(data.lastOnline);
        List onlineStatus = currentProxy == -1
                ? Arrays.asList(true, offlineSince)
                : Arrays.asList(false, currentServer, currentProxy, onlineSince);

        // send the information
        DisplayFormats.sendList(context, LanguageManager.get("playerinfo-header", playerName),
                context.getFormatSender(LanguageManager.get("playerinfo-entry"))
                        .addTranslated("playerinfo-entry-uuid", uuid)
                        .addTranslated("playerinfo-entry-ip", ip)
                        .addTranslated("playerinfo-entry-rank", MooQueries.getInstance().getGroupColor(rank) + rank)
                        .addTranslated("playerinfo-entry-coins", coins)
                        .addTranslated("playerinfo-entry-firstonline", firstOnline)
                        .addTranslated("playerinfo-entry-totalonline", totalOnline)
                        .addTranslated("playerinfo-entry-onlinestatus", onlineStatus)
                        .addTranslated("playerinfo-entry-banpoints", banPoints)
                        .addTranslated("playerinfo-entry-banstatus", currentBan != null, currentChatBan != null));
    }

}
