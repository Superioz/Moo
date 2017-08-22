package de.superioz.moo.proxy;

import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.events.CommandRegisterEvent;
import de.superioz.moo.api.io.JsonConfig;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.client.Moo;
import de.superioz.moo.client.events.CloudConnectedEvent;
import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import de.superioz.moo.proxy.command.PlayerParamType;
import de.superioz.moo.proxy.commands.*;
import de.superioz.moo.proxy.listeners.*;
import lombok.Getter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;

import java.util.Locale;

@Getter
public class ThunderPluginModule extends Module implements EventListener {

    private JsonConfig config;
    private LanguageManager languageManager;

    @Override
    public String getName() {
        return "thunder";
    }

    @Override
    protected void onEnable() {
        EventExecutor.getInstance().register(this);

        // load config
        this.config = Moo.getInstance().loadConfig(Thunder.getInstance().getDataFolder());
        this.languageManager = new LanguageManager(Thunder.getInstance().getDataFolder(), ChatUtil::fabulize);
        this.languageManager.load(Locale.US);

        // register commands
        CommandRegistry.getInstance().registerCommandsSeperately(
                new DatabaseModifyCommand(),
                new MooThunderCommand(), new TeamChatCommand(),
                new MaintenanceCommand(), new MotdCommand(),
                new GroupCommand(), new RankCommand(), new PermCommand(),
                new BanCommand(), new PunishInfoCommand(), new PunishArchiveCommand(),
                new UnbanCommand(), new KickCommand(),
                new WhoisCommand()
        );
        CommandRegistry.getInstance().getParamTypeRegistry().register(new PlayerParamType());

        // register handler
        Moo.getInstance().registerHandler(o -> {
                    if(o instanceof Listener) ProxyServer.getInstance().getPluginManager().registerListener(Thunder.getInstance(), (Listener) o);
                },
                new CommandListener(),
                new PacketPlayerKickListener(), new PacketPlayerMessageListener(),
                new PacketRequestListener(), new PacketConfigListener(),
                new PermissionListener(), new ProxyPingListener(),
                new ProxyPlayerLoginListener(), new ProxyPlayerConnectionListener(),
                new ServerRegisterChangeListener()
        );

        // connect to cloud
        if(config.isLoaded()) {
            Moo.getInstance().connect(config.get("proxy-name"), ClientType.PROXY,
                    config.get("cloud-ip"), config.get("cloud-port"));
        }
    }

    @Override
    protected void onDisable() {

    }

    @EventHandler
    public void onCommandRegister(CommandRegisterEvent event) {
        CommandInstance command = event.getInstance();
        Thunder.getInstance().getLogs().debugInfo("Register command: " + command.getLabel());

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
        Thunder.getInstance().getLogs().info("** AUTHENTICATION STATUS: " + (event.getStatus().getColored()) + " **");
        if(event.getStatus().isNok()) return;

        // lil
    }

}
