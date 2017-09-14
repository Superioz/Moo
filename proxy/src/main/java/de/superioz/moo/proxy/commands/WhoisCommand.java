package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.console.format.InfoListFormat;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.api.utils.TimeUtil;
import de.superioz.moo.network.common.MooPlayer;
import de.superioz.moo.network.common.MooProxy;
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
    public void onCommand(BungeeCommandContext context,ParamSet args) {
        String playerName = args.get(0);
        MooPlayer player = MooProxy.getPlayer(playerName);
        context.invalidArgument(!player.exists(), LanguageManager.get("error-player-doesnt-exist", playerName));

        // list current informations
        PlayerProfile profile = player.getProfile();
        Ban currentBan = profile.getCurrentBan();
        PlayerData data = profile.getData();

        // list rough information about the player
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
        context.sendDisplayFormat(new InfoListFormat().header("playerinfo-header", playerName).entryFormat("playerinfo-entry")
                .entry("playerinfo-entry-uuid", uuid)
                .entry("playerinfo-entry-ip", ip)
                .entry("playerinfo-entry-rank", player.getGroup().getColor() + rank)
                .entry("playerinfo-entry-coins", coins)
                .entry("playerinfo-entry-firstonline", firstOnline)
                .entry("playerinfo-entry-totalonline", totalOnline)
                .entry("playerinfo-entry-onlinestatus", onlineStatus)
                .entry("playerinfo-entry-banpoints", banPoints)
                .entry("playerinfo-entry-banstatus", currentBan != null)
        );
    }

}
