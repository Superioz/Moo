package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.minecraft.util.ChatUtil;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyPingListener implements Listener {

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        // list values for proxyping from config
        int maxPlayers = (int) MooCache.getInstance().getConfigEntry(NetworkConfigType.MAX_PLAYERS);
        int playerCount = (int) MooCache.getInstance().getConfigEntry(NetworkConfigType.PLAYER_COUNT);
        boolean maintenance = MooCache.getInstance().getConfigEntry(NetworkConfigType.MAINTENANCE).equals(true + "");

        // motd
        String motd = (String)MooCache.getInstance().getConfigEntry(maintenance ? NetworkConfigType.MAINTENANCE_MOTD : NetworkConfigType.MOTD);
        motd = motd.replace("\\n", "\n");

        // version
        // if maintenance other version then without
        if(maintenance) {
            // maintenance mode with wrong protocol version for a red X
            ping.setVersion(new ServerPing.Protocol(LanguageManager.get("maintenance-version"), 1305));
        }
        else {
            ping.setVersion(new ServerPing.Protocol(LanguageManager.get("default-version"), ping.getVersion().getProtocol()));
        }

        // set the players and the motd
        try {
            ping.setPlayers(new ServerPing.Players(maxPlayers, playerCount, ping.getPlayers().getSample()));
            ping.setDescriptionComponent(new TextComponent(TextComponent.fromLegacyText(ChatUtil.colored(motd))));

            // update ping
            event.setResponse(ping);
        }
        catch(Exception e) {
            System.err.println("Error while modifying ping of " + event.getConnection().getName() + "!");
        }
    }

}
