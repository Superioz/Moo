package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.client.events.PermissionUpdateEvent;
import de.superioz.moo.client.util.PermissionUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PermissionCheckEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;


public class PermissionListener implements Listener, EventListener {

    @EventHandler(priority = 127)
    public void onPermissionCheck(PermissionCheckEvent event) {
        CommandSender sender = event.getSender();
        String checkedPerm = event.getPermission();

        // if sender is player check permission
        // otherwise is the sender a console (= has always permission)
        boolean result = true;
        if(sender instanceof ProxiedPlayer) {
            result = PermissionUtil.hasPermission(checkedPerm, true,
                    ProxyCache.getInstance().getPermissions(((ProxiedPlayer) sender).getUniqueId()));
        }
        event.setHasPermission(result);
    }

    @de.superioz.moo.api.event.EventHandler
    public void onPermissionChange(PermissionUpdateEvent event) {
        // just updates the permissions
        Moo.getInstance().executeAsync(() -> {
            for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                ProxyCache.getInstance().updatePermission(player.getUniqueId());
            }
        });
    }

}
