package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketHandler;
import de.superioz.moo.protocol.packets.PacketConfig;
import net.md_5.bungee.api.ProxyServer;

public class PacketConfigListener implements PacketAdapter {

    @PacketHandler
    public void onConfig(PacketConfig packet) {
        PacketConfig.Type type = packet.type;
        PacketConfig.Command command = packet.command;
        String meta = packet.meta;

        // already handled the INFO case :)
        if(command == PacketConfig.Command.INFO) return;

        // after the maintenance mode changed, ..
        if(type == PacketConfig.Type.MAINTENANCE && meta.equals("true")) {
            int minRank = ProxyCache.getInstance().getConfigEntry(PacketConfig.Type.MAINTENANCE_RANK, Integer.class);

            // if the rank is too low, then prevent the player from joining
            ProxyServer.getInstance().getPlayers().forEach(player -> {
                if(MooQueries.getInstance().getGroup(player.getUniqueId()).rank < minRank) {
                    player.disconnect(LanguageManager.get("currently-in-maintenance"));
                }
            });
        }
    }

}
