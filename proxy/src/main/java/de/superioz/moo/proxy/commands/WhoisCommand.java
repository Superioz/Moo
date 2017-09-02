package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.DisplayFormats;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.network.common.MooQueries;
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
        PlayerProfile playerInfo = args.get(0, PlayerProfile.class);
        context.invalidArgument(playerInfo == null, LanguageManager.get("error-player-doesnt-exist", args.get(0)));

        // list current informations
        Ban currentBan = playerInfo.getCurrentBan();
        PlayerData data = playerInfo.getData();

        // list rough information about the player
        String playerName = args.get(0);
        UUID uuid = data.getUuid();
        String ip = data.getLastIp();
        String rank = data.getGroup();
        long coins = data.getCoins();
        int banPoints = data.getBanPoints();

        // list online status
        String firstOnline = TimeUtil.getFormat(data.getFirstOnline());
        String totalOnline = data.getTotalOnline() == null ? "0" : TimeUnit.MILLISECONDS.toHours(data.getTotalOnline()) + "h";
        String currentServer = data.getCurrentServer();
        int currentProxy = data.getCurrentProxy();
        long onlineSince = TimeUnit.MILLISECONDS.toMinutes(data.getCurrentOnline());
        String offlineSince = TimeUtil.getFormat(data.getLastOnline());
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
                        .addTranslated("playerinfo-entry-banstatus", currentBan != null));
    }

}
