package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.database.DatabaseType;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.client.Moo;
import de.superioz.moo.netty.common.MooQueries;
import de.superioz.moo.netty.packet.PacketAdapter;
import de.superioz.moo.netty.packet.PacketHandler;
import de.superioz.moo.netty.packets.PacketUpdatePermission;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PacketUpdatePermissionListener implements PacketAdapter {

    @PacketHandler
    public void onUpdatePermission(PacketUpdatePermission packet) {
        DatabaseType type = packet.type;
        String key = packet.key;

        if(type == DatabaseType.PLAYER) {
            // update ones player permission
            UUID uuid = Validation.UNIQUEID.matches(key)
                    ? UUID.fromString(key)
                    : (ProxyServer.getInstance().getPlayer(key) == null ? null : ProxyServer.getInstance().getPlayer(key).getUniqueId());
            if(uuid != null) {
                Moo.getInstance().runAsync((Runnable) () -> MooQueries.getInstance().updatePermission(uuid));
            }
        }
        else if(type == DatabaseType.GROUP) {
            // update every player in this group
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()){
                UUID uuid = player.getUniqueId();
                PlayerData data = MooCache.getInstance().getUniqueIdPlayerMap().get(uuid);

                if(data == null || !data.getGroup().equals(key)) continue;
                Moo.getInstance().runAsync((Runnable) () -> MooQueries.getInstance().updatePermission(uuid));
            }
        }
    }

}
