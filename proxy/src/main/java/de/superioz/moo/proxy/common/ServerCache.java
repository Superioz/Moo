package de.superioz.moo.proxy.common;

import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ServerCache {

    public static ServerCache getInstance() {
        if(instance == null) instance = new ServerCache();
        return instance;
    }

    private static ServerCache instance;

    @Getter
    private Map<ServerInfo, ServerSpecificInfo> cachedServer = new HashMap<>();

    /**
     * Gets the server where the regex matches
     *
     * @param regex The regex
     * @return The list of servers which name matches
     */
    public List<ServerInfo> getServer(String regex) {
        List<ServerInfo> list = new ArrayList<>();
        Pattern p = Pattern.compile(regex);

        for(ServerInfo server : cachedServer.keySet()) {
            if(p.matcher(server.getName()).matches()) list.add(server);
        }
        return list;
    }

    /**
     * Updates the server address
     *
     * @param address            The address
     * @param serverSpecificInfo The server info
     */
    public void updateServer(InetSocketAddress address, ServerSpecificInfo serverSpecificInfo) {
        // get server with given address
        ServerInfo info = null;
        for(ServerInfo s : ProxyServer.getInstance().getServers().values()) {
            if(s.getAddress().equals(address)) {
                info = s;
            }
        }

        // check info
        if(info == null) {
            // server doesnt exist
            return;
        }

        cachedServer.put(info, serverSpecificInfo);
    }

}
