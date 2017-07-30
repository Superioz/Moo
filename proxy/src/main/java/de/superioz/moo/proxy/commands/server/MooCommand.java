package de.superioz.moo.proxy.commands.server;

import de.superioz.moo.api.collection.MultiMap;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packet.AbstractPacket;
import de.superioz.moo.protocol.packets.PacketServerRequest;
import de.superioz.moo.protocol.packets.PacketServerRequestShutdown;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Map;
import java.util.function.Consumer;

public class MooCommand {

    /**
     * Checks if the connection to the cloud has been activated/deactivated
     *
     * @param context The context for sending message
     * @return Result
     */
    private boolean checkMoo(CommandContext context) {
        boolean activated = Moo.getInstance().isActivated();

        if(!activated) {
            context.sendMessage("&4The connection to the cloud has been deactivated in the config. " +
                    "Please enable it before using this command.");
        }
        return activated;
    }

    @RunAsynchronous
    @Command(label = "moo",
            permission = "network.moo",
            usage = "[subCommand]")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        boolean connected = Moo.getInstance().isConnected();

        if(!checkMoo(context)) return;

        // get status from connection
        context.sendMessage("&7Connection status: " + (connected ? "&aONLINE" : "&8Offline"));
        if(connected) {
            context.sendMessage("&7Cloud-Communication-System &bMoo &7v&f"
                    + Moo.getInstance().getClient().getMasterVersion()
                    + " &7(Ping: " + Moo.getInstance().ping() + "ms)");
        }
    }

    @RunAsynchronous
    @Command(label = "connect", parent = "moo")
    public void connect(BungeeCommandContext context, ParamSet args) {
        if(!checkMoo(context)) return;

        // if the connection is already up
        if(Moo.getInstance().isConnected()) {
            context.sendMessage("&7This instance is already connected to the cloud.");
            return;
        }
        context.sendMessage("&7Connecting to the cloud ..");
        Moo.getInstance().reconnect();
    }

    @RunAsynchronous
    @Command(label = "disconnect", parent = "moo")
    public void disconnect(BungeeCommandContext context, ParamSet args) {
        if(!checkMoo(context)) return;

        // if the connection is already disconnected
        if(!Moo.getInstance().isConnected()) {
            context.sendMessage("&7This instance is not connected to the cloud.");
            return;
        }
        context.sendMessage("&7Disconnecting from the cloud ..");
        Moo.getInstance().disconnect();
    }

    @RunAsynchronous
    @Command(label = "listserver", parent = "moo", flags = "r")
    public void listserver(BungeeCommandContext context, ParamSet args) {
        Map<String, ServerInfo> serverMap = ProxyServer.getInstance().getServers();

        // if no server is connected to the bungee
        if(serverMap.isEmpty()) {
            context.sendMessage("&cNo server available to display!");
            return;
        }

        // send raw server information (not recommendend for a mass of servers)
        if(args.hasFlag("r")) {
            context.sendMessage("&6Server(s)&7: {"
                    + StringUtil.getListToString(serverMap.keySet(), "&8,&7", s -> s)
                    + "&7}");
            return;
        }

        // otherwise categorize the servers and display them differently
        MultiMap<String, ServerInfo> categoryServerMap = new MultiMap<>();
        serverMap.forEach((s, serverInfo) -> categoryServerMap
                .add(s.replaceAll("[1-9_.#\\- ]", "").toLowerCase(), serverInfo));

        for(String category : categoryServerMap.keySet()) {
            context.sendMessage("&8# &c" + categoryServerMap.get(category).size() + " &7" + category + " server registered.");
        }
    }

    @RunAsynchronous
    @Command(label = "reqserver", parent = "moo", usage = "<type> [amount]", flags = "s")
    public void reqserver(BungeeCommandContext context, ParamSet args) {
        String type = args.get(0);
        int amount = args.getInt(1, 1);

        // check parameter
        // also check the amount (less than 1 is crap and above 10 as well)
        if(type == null || amount < 1 || amount > 10) {
            context.sendMessage("&cInvalid server type or amount! (t:" + type + "; a:" + amount + ")");
            return;
        }

        // build packet and send it to the cloud
        context.sendMessage("Requesting server to start .. (" + amount + "x " + type + ")");
        PacketMessenger.transfer(new PacketServerRequest(type, args.hasFlag("s"), amount), new Consumer<AbstractPacket>() {
            @Override
            public void accept(AbstractPacket abstractPacket) {
                context.sendMessage(abstractPacket.toString());
            }
        });
    }

    @RunAsynchronous
    @Command(label = "reqshutdown", parent = "moo", usage = "<host> <port>")
    public void reqshutdown(BungeeCommandContext context, ParamSet args) {
        String host = args.get(0);
        int port = args.getInt(1, -1);

        // check parameter
        if(host == null || port < 0) {
            context.sendMessage("&cInvalid host or port! (h:" + host + "; p:" + port + ")");
            return;
        }

        // build packet and send it to the cloud
        context.sendMessage("Requesting a server to shutdown .. (" + host + ":" + port + ")");
        PacketMessenger.transfer(new PacketServerRequestShutdown(host, port), new Consumer<AbstractPacket>() {
            @Override
            public void accept(AbstractPacket abstractPacket) {
                context.sendMessage(abstractPacket.toString());
            }
        });
    }

}
