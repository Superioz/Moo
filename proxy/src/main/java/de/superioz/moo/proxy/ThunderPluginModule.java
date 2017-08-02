package de.superioz.moo.proxy;

import de.superioz.moo.proxy.command.PlayerParamType;
import de.superioz.moo.proxy.commands.*;
import de.superioz.moo.proxy.commands.player.WhoisCommand;
import de.superioz.moo.proxy.commands.punishment.*;
import de.superioz.moo.proxy.commands.rank.GroupCommand;
import de.superioz.moo.proxy.commands.rank.PermCommand;
import de.superioz.moo.proxy.commands.rank.RankCommand;
import de.superioz.moo.proxy.commands.server.MaintenanceCommand;
import de.superioz.moo.proxy.commands.server.MotdCommand;
import de.superioz.moo.proxy.listeners.*;
import lombok.Getter;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.CommandRegisterEvent;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.common.MooPlugin;
import de.superioz.moo.client.common.MooPluginStartup;
import de.superioz.moo.client.common.ProxyCache;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.client.util.MooPluginUtil;
import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.commands.server.MooCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

import java.io.File;
import java.util.Locale;
import java.util.function.Function;

@Getter
public class ThunderPluginModule extends Module implements EventListener, MooPlugin {

    private JsonConfig config;
    private LanguageManager languageManager;

    @Override
    public String getName() {
        return "thunder";
    }

    @Override
    protected void onEnable() {
        EventExecutor.getInstance().register(this);

        if(config.isLoaded()) {
            Moo.getInstance().connect(config.get("proxy-name"), ClientType.PROXY,
                    config.get("cloud-ip"), config.get("cloud-port"));
        }
    }

    @Override
    protected void onDisable() {

    }

    @Override
    public void loadConfig() {
        File folder = Thunder.getInstance().getDataFolder();
        config = MooPluginUtil.loadConfig(folder, "config");

        Thunder.getLogs().info("Loading properties ..");
        languageManager = new LanguageManager(folder, ChatUtil::fabulize);
        languageManager.load(Locale.US);
    }

    @Override
    public Function<Object, Boolean> registerLeftOvers() {
        return object -> {
            if(object instanceof Listener) {
                ProxyServer.getInstance().getPluginManager().registerListener(Thunder.getInstance(), (Listener) object);
            }
            return false;
        };
    }

    @Override
    public void loadPluginStartup(MooPluginStartup startup) {
        startup.registerCommands(
                new DatabaseModifyCommand(),
                new MooCommand(), new TeamCommand(), new TeamChatCommand(),
                new MaintenanceCommand(), new MotdCommand(),
                new GroupCommand(), new RankCommand(), new PermCommand(),
                new BanCommand(), new PunishInfoCommand(),
                new UnbanCommand(), new KickCommand(),
                new WhoisCommand()
        );
        startup.registerListeners(this,
                new CommandListener(),
                new PacketPlayerKickListener(), new PacketPlayerMessageListener(),
                new PacketRequestListener(), new PacketRespondListener(), new PermissionListener(),
                new ProxyPingListener(), new PacketConfigListener(),
                new ProxyPlayerLoginListener(), new ProxyPlayerConnectListener(),
                new ServerRegisterChangeListener());
        startup.registerTypes(new PlayerParamType());
    }

    @EventHandler
    public void onCommandRegister(CommandRegisterEvent event) {
        CommandInstance command = event.getInstance();
        Thunder.getLogs().debugInfo("Register command: " + command.getLabel());

        Command bungeeCommand = new Command(command.getLabel(), command.getPermission(),
                command.getAliases().toArray(new String[]{})) {
            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                command.execute(new BungeeCommandContext(commandSender), strings);
            }
        };
        Thunder.getInstance().getProxy().getPluginManager().registerCommand(Thunder.getInstance(), bungeeCommand);
    }

    @EventHandler
    public void onStart(CloudConnectedEvent event) {
        Thunder.getLogs().info("** AUTHENTICATION STATUS: " + (event.getStatus().getColored()) + " **");
        if(event.getStatus().isNok()) return;

        // load groups
        ProxyServer.getInstance().getScheduler().runAsync(Thunder.getInstance(), () -> ProxyCache.getInstance().loadGroups());

        // load config
        Moo.getInstance().loadProxyConfig();
    }

}
