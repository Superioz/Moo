package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.common.PlayerInfo;
import de.superioz.moo.api.database.object.Ban;
import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.api.database.object.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.io.MooConfigType;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.protocol.exception.MooOutputException;
import de.superioz.moo.protocol.packets.PacketPlayerState;
import de.superioz.moo.proxy.Thunder;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class ProxyPlayerLoginListener implements Listener {

    /**
     * During the login event of the player<br>
     * You have to complete the intent of spigot ({@link LoginEvent#completeIntent(Plugin)}) to be executed async
     *
     * @param event The event
     */
    private void onLoginAsync(LoginEvent event) {
        // if the cloud is not activated then just skip this event
        if(!Moo.getInstance().isActivated()) {
            event.completeIntent(Thunder.getInstance());
            return;
        }

        // if the cloud is activated
        // checks if the client is connected to the cloud
        if(!Moo.getInstance().isConnected()) {
            event.setCancelReason(LanguageManager.get("cancel-reason-offline-cloud"));
            event.setCancelled(true);
            event.completeIntent(Thunder.getInstance());
            return;
        }

        // values from the event
        PendingConnection connection = event.getConnection();
        UUID uuid = connection.getUniqueId();

        // get player info
        // check if the player is banned or whatever
        // also archive bans if the ban ran out
        // ...
        PlayerInfo playerInfo = MooQueries.getInstance().getPlayerInfo(uuid);
        if(playerInfo == null) {
            event.completeIntent(Thunder.getInstance());
            return;
        }

        // checks if the player is banned
        // if ban ran out archive it otherwise cancel login
        Ban ban = playerInfo.getCurrentBan();
        if(ban != null) {
            long stamp = ban.start + ban.duration;
            if(ban.duration != -1 && stamp < System.currentTimeMillis()) {
                // ban ran out; please archive it
                MooQueries.getInstance().archiveBan(ban);
            }
            else {
                // ban is active
                event.setCancelReason(ban.apply(LanguageManager.get(ban.isPermanent() ? "ban-message-perm" : "ban-message-temp")));
                event.setCancelled(true);
            }
        }

        // get group for checking the maintenance bypassability
        Group group = MooQueries.getInstance().getGroup(playerInfo.getData().group);
        boolean maintenanceBypass = group.rank >= (int) MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE_RANK);

        // if maintenance mode is active and the player is not allowed to bypass it
        if(MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE).equals(true + "") && !maintenanceBypass) {
            event.setCancelReason(LanguageManager.get("currently-in-maintenance"));
            event.setCancelled(true);
        }
        event.completeIntent(Thunder.getInstance());
    }

    @EventHandler
    public void onLogin(LoginEvent event) {
        if(event.isCancelled()) return;

        event.registerIntent(Thunder.getInstance());
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onLoginAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Before the login event of the player
     *
     * @param event The event
     */
    private void onPostLoginAsync(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();

        // create playerdata with every important information
        PlayerData data = new PlayerData();
        data.uuid = player.getUniqueId();
        data.lastName = player.getName();
        data.lastip = player.getAddress().getHostString();

        // changes the state of the player
        // if the respond was successful put the deserialized values into the proxycache
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.JOIN_PROXY, response -> {
            if(response.isOk()) {
                // only update the permission, the rest has been updated before
                MooQueries.getInstance().updatePermission(data.uuid);
            }
        });
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        Thunder.getInstance().getProxy().getScheduler().runAsync(Thunder.getInstance(), () -> {
            try {
                onPostLoginAsync(event);
            }
            catch(MooOutputException e) {
                e.printStackTrace();
            }
        });
    }

}
