package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.protocol.packets.PacketConfig;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyPingListener implements Listener {

    @EventHandler
    public void onPing(ProxyPingEvent event) {
        ServerPing ping = event.getResponse();

        // get values for proxyping from config
        int maxPlayers = ProxyCache.getInstance().getConfigEntry(PacketConfig.Type.MAX_PLAYERS, Integer.class);
        int playerCount = ProxyCache.getInstance().getConfigEntry(PacketConfig.Type.PLAYER_COUNT, Integer.class);
        boolean maintenance = ProxyCache.getInstance().getConfigEntry(PacketConfig.Type.MAINTENANCE).equals(true + "");

        // motd
        String motd = ProxyCache.getInstance().getConfigEntry(maintenance ? PacketConfig.Type.MAINTENANCE_MOTD : PacketConfig.Type.MOTD);
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
