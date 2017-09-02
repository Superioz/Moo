package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.client.Moo;
import de.superioz.moo.network.common.MooQueries;
import de.superioz.moo.network.exception.MooOutputException;
import de.superioz.moo.network.packets.PacketPlayerState;
import de.superioz.moo.proxy.Thunder;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

public class ProxyPlayerLoginListener implements Listener {

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
     * During the login event of the player<br>
     * You have to complete the intent of spigot ({@link LoginEvent#completeIntent(Plugin)}) to be executed async
     *
     * @param event The event
     */
    private void onLoginAsync(LoginEvent event) {
        // if the cloud is not activated then just skip this event
        if(!Moo.getInstance().isEnabled() || !Moo.getInstance().isConnected()) {
            event.completeIntent(Thunder.getInstance());
            return;
        }

        // if the cloud is activated
        // checks if the client is connected to the cloud
        if(!Moo.getInstance().isConnected()) {
            event.setCancelReason(LanguageManager.get("error-reason-offline-cloud"));
            event.setCancelled(true);
            event.completeIntent(Thunder.getInstance());
            return;
        }

        // values from the event
        PendingConnection connection = event.getConnection();
        UUID uuid = connection.getUniqueId();

        // create playerdata with every important information
        PlayerData data = new PlayerData();
        data.setUuid(connection.getUniqueId());
        data.setLastName(connection.getName());
        data.setLastIp(connection.getAddress().getHostString());

        // changes state
        MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.LOGIN_PROXY, response -> {

            // check can join
            Thunder.getInstance().checkPlayerProfileBeforeLogin(uuid, event);

            // check if intent is completed
            if(!event.getIntents().contains(Thunder.getInstance())){
                return;
            }

            // change player state for current server, proxy, ..
            MooQueries.getInstance().changePlayerState(data, PacketPlayerState.State.JOIN_PROXY, response2 -> {
                MooQueries.getInstance().updatePermission(data.getUuid());
            });

            // complete
            event.completeIntent(Thunder.getInstance());
        });
    }

}
