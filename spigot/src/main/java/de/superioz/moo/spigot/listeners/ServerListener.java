package de.superioz.moo.spigot.listeners;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.protocol.packets.PacketPlayerState;
import de.superioz.moo.spigot.common.CustomPermissible;
import de.superioz.moo.spigot.common.PermissionInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.Permissible;

public class ServerListener implements Listener {

    /**
     * When the player joins the server
     * <p>
     * SERVER JOIN
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if(!Moo.getInstance().isConnected()) return;
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastIp = player.getAddress().getHostString();

        // changes state
        Permissible oldPermissible = PermissionInjector.getPermissible(player);
        CustomPermissible customPermissible = new CustomPermissible(player, data.uuid, oldPermissible);
        PermissionInjector.inject(player, customPermissible);

        // SET JOIN MESSAGE
        String playerName = MooQueries.getInstance().getGroup(player.getUniqueId()).color + player.getName();
        Bukkit.getServer().broadcastMessage(LanguageManager.get("join-message-pattern", playerName));

        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.JOIN_SERVER, response -> {
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        if(!Moo.getInstance().isConnected()) return;
        Player player = event.getPlayer();

        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastIp = player.getAddress().getHostString();

        // SET QUIT MESSAGE
        String playerName = MooQueries.getInstance().getGroup(player.getUniqueId()).color + player.getName();
        event.setQuitMessage(LanguageManager.get("quit-message-pattern", playerName));
    }

}
