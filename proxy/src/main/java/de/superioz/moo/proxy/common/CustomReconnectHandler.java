package de.superioz.moo.proxy.common;

import de.superioz.moo.proxy.Thunder;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class CustomReconnectHandler implements ReconnectHandler {

    @Override
    public ServerInfo getServer(ProxiedPlayer proxiedPlayer) {
        // here we gonna get the server the player will be forced to on login
        List<ServerInfo> l = Thunder.getInstance().getServers(Thunder.LOBBY_REGEX);

        // if no lobby server is registered
        if(l.isEmpty()) {
            System.out.println("No lobby registered!");
            return null;
        }

        // list lobby with fewest player online
        ServerInfo lobbyFewest = null;
        int fewestPlayer = -1;
        for(ServerInfo serverInfo : l) {
            if(fewestPlayer == -1 || serverInfo.getPlayers().size() < fewestPlayer) {
                fewestPlayer = serverInfo.getPlayers().size();
                lobbyFewest = serverInfo;
            }
        }
        if(lobbyFewest == null) {
            // rip
            return null;
        }
        return lobbyFewest;
    }

    @Override
    public void setServer(ProxiedPlayer proxiedPlayer) {
        // we don't want to set the server, so that the player can't reconnect to a server
    }

    @Override
    public void save() {
        // no reconnection
    }

    @Override
    public void close() {
        // no reconnection
    }
}
