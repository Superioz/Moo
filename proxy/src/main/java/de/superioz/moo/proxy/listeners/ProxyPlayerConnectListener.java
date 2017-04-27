package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.protocol.common.Response;
import de.superioz.moo.protocol.exception.MooOutputException;
import de.superioz.moo.protocol.packets.PacketPlayerState;
import de.superioz.moo.proxy.Thunder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyPlayerConnectListener implements Listener {

    /**
     * When the player is about connecting to a server
     * TODO forward to free lobby
     *
     * @param event The event
     */
    private void onServerConnectAsync(ServerConnectEvent event) {
        /*if(!event.getTarget().getName().contains("lobby")) return;
        List<ServerInfo> l = Thunder.getServers("(lobby-[0-9]*)");

        // get random number (random lobby)
        int number = NumberUtil.getRandom(0, l.size() - 1);
        if(number >= l.size() || number < 0) return;
        ServerInfo target = l.get(number);

        // set the new server (a random lobby)
        if(target != null) {
            event.setTarget(target);
        }*/
    }

    @EventHandler
    public void onServerConnect(ServerConnectEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onServerConnectAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
                //
            }
        });
    }

    /**
     * When the player connected to a server
     *
     * @param event The event
     */
    private void onServerConnectedAsync(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // get playerdata for updating state
        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastip = player.getAddress().getHostString();

        // changes the state of the player
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.CONNECT_SERVER,
                event.getServer().getInfo().getName());
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> onServerConnectedAsync(event));
    }

    /**
     * When the player disconnected from the server
     *
     * @param event The event
     */
    private void onPlayerDisconnectAsync(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // get playerdata for updating state
        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastip = player.getAddress().getHostString();

        // changes the player's state; removes player data
        Response respond = MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.LEAVE_PROXY);
        if(respond.isOk()) {
            ProxyCache.getInstance().remove(data);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onPlayerDisconnectAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
                //
            }
        });
    }

}
