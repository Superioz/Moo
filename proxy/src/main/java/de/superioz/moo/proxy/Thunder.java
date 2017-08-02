package de.superioz.moo.proxy;

import lombok.Getter;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.logging.Logs;
import de.superioz.moo.api.module.ModuleRegistry;
import de.superioz.moo.client.Moo;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Getter
public class Thunder extends Plugin implements EventListener {

    public static final String LOBBY_NAME = "lobby";
    public static final String LIMBO_NAME = "limbo";
    public static final String LOBBY_REGEX = "(" + LOBBY_NAME + "-[0-9]*)|(" + LOBBY_NAME + ")";

    @Getter
    private static Thunder instance;
    @Getter
    private static Logs logs;

    private ModuleRegistry moduleRegistry;
    private ThunderPluginModule pluginModule;
    private JsonConfig config;

    @Override
    public void onEnable() {
        instance = this;

        // logging
        logs = new Logs(getLogger());
        logs.prepareNativeStreams().enableFileLogging();

        // initialise moo and plugin module
        this.pluginModule = new ThunderPluginModule();
        Moo.initialise(pluginModule, getLogger());

        this.moduleRegistry = new ModuleRegistry(logs);
        this.moduleRegistry.register(pluginModule);
        this.config = pluginModule.getConfig();
        logs.setDebugMode(config.get("debug"));
        logs.info("Debug Mode is " + (logs.isDebugMode() ? "ON" : "off"));

        // we don't want pre-defined servers! dk if its event possible to block them completely :thinking:
        int preDefinedServerSize = ProxyServer.getInstance().getServers().size();
        if(preDefinedServerSize != 0) {
            logs.info("There is " + preDefinedServerSize + " server predefined which could lead to errors!");
        }
    }

    @Override
    public void onDisable() {
        // .
        logs.disable();
        moduleRegistry.disableAll();
        Moo.getInstance().disconnect();
    }

    /**
     * Registers a server
     *
     * @param name       The name of the server
     * @param host       The host
     * @param port       The port
     * @param motd       The motd
     * @param restricted Is the server restricted
     * @return The server
     */
    public static ServerInfo registerServer(String name, String host, int port, String motd, boolean restricted) {
        if(ProxyServer.getInstance().getServers().containsKey(name)) return ProxyServer.getInstance().getServers().get(name);

        //
        InetSocketAddress address = InetSocketAddress.createUnresolved(host, port);
        ServerInfo info = ProxyServer.getInstance().constructServerInfo(name, address, motd, restricted);
        return ProxyServer.getInstance().getServers().put(name, info);
    }

    public static ServerInfo registerServer(String name, String host, int port) {
        int similar = Thunder.getServers("(" + name + "-[0-9]*|" + name + ")").size();
        name = similar == 0 ? name : name + "-" + (similar + 1);

        getLogs().debugInfo("Register server '" + name + "'(" + host + ":" + port + ") ..");

        return Thunder.registerServer(name, host, port, "", false);
    }

    /**
     * Get servers where the name matches given regex
     *
     * @param regex The regex
     * @return The list of serverInfos
     */
    public static List<ServerInfo> getServers(String regex) {
        List<ServerInfo> list = new ArrayList<>();
        Pattern p = Pattern.compile(regex);

        for(ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if(p.matcher(server.getName()).matches()) list.add(server);
        }
        return list;
    }

    /**
     * Unregisters a server info
     *
     * @param host The host
     * @param port The port
     * @return The server
     */
    public static ServerInfo unregisterServer(String host, int port) {
        getLogs().debugInfo("Unregister server (" + host + ":" + port + ") ..");

        ServerInfo info = null;
        for(ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            InetSocketAddress address = (info = server).getAddress();
            if(address.getHostName().equals(host)
                    && address.getPort() == port) {
                break;
            }
        }

        if(info != null) {
            ProxyServer.getInstance().getServers().remove(info.getName());
        }
        return info;
    }

}
