package de.superioz.moo.spigot.listeners;

import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.protocol.exception.MooOutputException;
import de.superioz.moo.protocol.packets.PacketPlayerState;
import de.superioz.moo.spigot.common.CustomPermissible;
import de.superioz.moo.spigot.common.PermissionInjector;
import de.superioz.moo.spigot.util.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;

public class ServerListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(PlayerJoinEvent event) {
        try {
            onJoin02(event);
        }
        catch(MooOutputException e) {
            //
        }
    }

    /**
     * When the player joins the server
     * <p>
     * SERVER JOIN
     */
    private void onJoin02(PlayerJoinEvent event)  {
        if(!Moo.getInstance().isConnected()) return;
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastip = player.getAddress().getHostString();

        // changes state
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.JOIN_SERVER, response -> {
            if(response.isNotOk()) return;

            Permissible oldPermissible = PermissionInjector.getPermissible(player);
            CustomPermissible customPermissible = new CustomPermissible(player, data.uuid, oldPermissible);
            PermissionInjector.inject(player, customPermissible);

            // SET JOIN MESSAGE
            String playerName = MooQueries.getInstance().getGroup(player.getUniqueId()).color + player.getName();
            Bukkit.getServer().broadcastMessage(LanguageManager.get("join-message-pattern", playerName));
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLeave(PlayerQuitEvent event) {
        if(!Moo.getInstance().isConnected()) return;
        Player player = event.getPlayer();

        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastip = player.getAddress().getHostString();

        // SET QUIT MESSAGE
        String playerName = MooQueries.getInstance().getGroup(player.getUniqueId()).color + player.getName();
        event.setQuitMessage(LanguageManager.get("quit-message-pattern", playerName));
    }

}
