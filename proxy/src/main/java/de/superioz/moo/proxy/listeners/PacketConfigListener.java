package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.redis.MooCache;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.config.NetworkConfigType;
import de.superioz.moo.network.queries.MooQueries;
import de.superioz.moo.network.packet.PacketAdapter;
import de.superioz.moo.network.packet.PacketHandler;
import de.superioz.moo.network.packets.PacketConfig;
import net.md_5.bungee.api.ProxyServer;

public class PacketConfigListener implements PacketAdapter {

    @PacketHandler
    public void onConfig(PacketConfig packet) {
        NetworkConfigType type = packet.type;
        String meta = packet.meta;

        // after the maintenance mode changed kick all players who're not permitted
        if(type == NetworkConfigType.MAINTENANCE && meta.equals(true + "")) {
            // list maintenance rank from redis
            int minRank = MooCache.getInstance().getConfigEntry(NetworkConfigType.TEAM_RANK);

            ProxyServer.getInstance().getPlayers().forEach(player -> {
                if(MooQueries.getInstance().getPlayerData(player.getUniqueId()).getRank() < minRank) {
                    player.disconnect(LanguageManager.get("error-currently-in-maintenance"));
                }
            });
        }
    }

}
