package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.config.MooConfigType;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketConfig;
import net.md_5.bungee.api.ProxyServer;

public class PacketConfigListener implements PacketAdapter {

    @PacketHandler
    public void onConfig(PacketConfig packet) {
        MooConfigType type = packet.type;
        String meta = packet.meta;

        // after the maintenance mode changed kick all players who're not permitted
        if(type == MooConfigType.MAINTENANCE && meta.equals(true + "")) {
            // list maintenance rank from redis
            int minRank = (int) MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE_RANK);

            ProxyServer.getInstance().getPlayers().forEach(player -> {
                if(MooQueries.getInstance().getPlayerData(player.getUniqueId()).getRank() < minRank) {
                    player.disconnect(LanguageManager.get("error-currently-in-maintenance"));
                }
            });
        }
    }

}
