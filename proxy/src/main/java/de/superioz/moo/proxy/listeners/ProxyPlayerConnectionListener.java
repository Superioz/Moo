package de.superioz.moo.proxy.listeners;

import de.superioz.moo.network.redis.MooCache;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.network.common.MooQueries;
import de.superioz.moo.network.exception.MooOutputException;
import de.superioz.moo.network.packets.PacketPlayerState;
import de.superioz.moo.proxy.Thunder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyPlayerConnectionListener implements Listener {

    /**
     * When the player connected to a server
     *
     * @param event The event
     */
    private void onServerConnectedAsync(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // list playerdata for updating state
        PlayerData data = new PlayerData();
        data.setUuid(player.getUniqueId());
        data.setLastName(player.getName());
        data.setLastIp(player.getAddress().getHostString());

        // changes the state of the player
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.CONNECT_SERVER, event.getServer().getInfo().getName(), response -> {
        });
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onServerConnectedAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * When the player disconnected from the server
     *
     * @param event The event
     */
    private void onPlayerDisconnectAsync(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // list playerdata for updating state
        PlayerData data = new PlayerData();
        data.setUuid(player.getUniqueId());
        data.setLastName(player.getName());
        data.setLastIp(player.getAddress().getHostString());

        // changes the player's state; removes player data
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.LEAVE_PROXY, response -> {
            if(response.isOk()) {
                try {
                    // we wait 1s because the player has to leave properly
                    Thread.sleep(1000L);
                }
                catch(InterruptedException e) {
                    //
                }

                // removes data of player after 1s
                MooCache.getInstance().getPlayerPermissionMap().removeAsync(data.getUuid());
                MooCache.getInstance().getPlayerMap().removeAsync(data.getUuid());
                MooCache.getInstance().getNameUniqueIdMap().removeAsync(data.getLastName());
            }
        });
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onPlayerDisconnectAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
            }
        });
    }

}
