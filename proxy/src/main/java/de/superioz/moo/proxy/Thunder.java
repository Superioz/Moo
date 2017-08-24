package de.superioz.moo.proxy;

import de.superioz.moo.api.cache.MooCache;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.config.MooConfigType;
import de.superioz.moo.api.database.objects.Ban;
import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.io.CustomFile;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.logging.Loogger;
import de.superioz.moo.api.module.ModuleRegistry;
import de.superioz.moo.api.modules.RedisModule;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooQueries;
import lombok.Getter;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Getter
public class Thunder extends Plugin implements EventListener {

    public static final String LOBBY_NAME = "lobby";
    public static final String LIMBO_NAME = "limbo";
    public static final String LOBBY_REGEX = "(" + LOBBY_NAME + "-[0-9]*)|(" + LOBBY_NAME + ")";

    @Getter
    private static Thunder instance;
    private Loogger logs;

    private ModuleRegistry moduleRegistry;
    private ThunderPluginModule pluginModule;

    @Override
    public void onEnable() {
        instance = this;

        // initialize moo and modules
        Moo.initialize((logs = new Loogger(getLogger())).prepareNativeStreams().enableFileLogging().getBaseLogger());
        this.pluginModule = new ThunderPluginModule();
        this.moduleRegistry = new ModuleRegistry(logs);
        this.moduleRegistry.register(pluginModule);
        this.pluginModule.waitFor(module -> {
            if(module.getErrorReason() != null) return;
            CustomFile customFile = new CustomFile(((ThunderPluginModule) module).getConfig().get("redis-config"), Paths.get("configuration"));
            customFile.load(true, true);
            moduleRegistry.register(new RedisModule(customFile.getFile(), getLogger()));
        });

        // list config
        logs.setDebugMode(pluginModule.getConfig().get("debug"));
        logs.info("Debug Mode is " + (logs.isDebugMode() ? "ON" : "off"));

        // we don't want pre-defined servers! dk if its event possible to block them completely :thinking:
        int preDefinedServerSize = ProxyServer.getInstance().getServers().size();
        if(preDefinedServerSize != 0) {
            logs.debug("There is " + preDefinedServerSize + " server predefined.");
        }

        // summary?
        moduleRegistry.sendModuleSummaryAsync();
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
    public ServerInfo registerServer(String name, String host, int port, String motd, boolean restricted) {
        if(ProxyServer.getInstance().getServers().containsKey(name)) return ProxyServer.getInstance().getServers().get(name);

        //
        InetSocketAddress address = InetSocketAddress.createUnresolved(host, port);
        ServerInfo info = ProxyServer.getInstance().constructServerInfo(name, address, motd, restricted);
        return ProxyServer.getInstance().getServers().put(name, info);
    }

    public ServerInfo registerServer(String name, String host, int port) {
        int similar = getServers("(" + name + "-[0-9]*|" + name + ")").size();
        name = similar == 0 ? name : name + "-" + (similar + 1);

        getLogs().debugInfo("Register server '" + name + "'(" + host + ":" + port + ") ..");

        return registerServer(name, host, port, "", false);
    }

    /**
     * Get servers where the name matches given regex
     *
     * @param regex The regex
     * @return The list of serverInfos
     */
    public List<ServerInfo> getServers(String regex) {
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
    public ServerInfo unregisterServer(String host, int port) {
        ServerInfo info = null;
        for(ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            InetSocketAddress address = (info = server).getAddress();
            if(address.getHostName().equals(host)
                    && address.getPort() == port) {
                // send message only if really one server has been found to remove
                getLogs().debugInfo("Unregister server (" + host + ":" + port + ") ..");
                break;
            }
        }

        if(info != null) {
            ProxyServer.getInstance().getServers().remove(info.getName());
        }
        return info;
    }

    /**
     * Checks given uuid (gets the playerProfile) if the player can join.
     *
     * @param uuid  The uniqueid of the player
     * @param event The event to be cancelled if he can't
     */
    public void checkPlayerProfileBeforeLogin(UUID uuid, LoginEvent event) {
        // list player info
        // check if the player is banned or whatever
        // also archive bans if the ban ran out
        // ...
        PlayerProfile playerProfile = MooQueries.getInstance().getPlayerProfile(uuid);
        if(playerProfile == null) {
            event.completeIntent(Thunder.getInstance());
            return;
        }

        // checks if the player is banned
        // if ban ran out archive it otherwise cancel login
        Ban ban = playerProfile.getCurrentBan();
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

        // list group for checking the maintenance bypassability
        Group group = MooQueries.getInstance().getGroup(playerProfile.getData().group);
        boolean maintenanceBypass = group.rank >= (int) MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE_RANK);

        // if maintenance mode is active and the player is not allowed to bypass it
        if(MooCache.getInstance().getConfigEntry(MooConfigType.MAINTENANCE).equals(true + "") && !maintenanceBypass) {
            event.setCancelReason(LanguageManager.get("currently-in-maintenance"));
            event.setCancelled(true);
        }
    }

}
