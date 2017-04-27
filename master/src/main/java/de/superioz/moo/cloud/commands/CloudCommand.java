package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.common.MooPlayer;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.exceptions.InvalidConfigException;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketConfig;
import de.superioz.moo.protocol.packets.PacketKeepalive;
import de.superioz.moo.protocol.server.MooClient;

import java.util.ArrayList;
import java.util.List;

public class CloudCommand implements EventListener {

    @Command(label = "config")
    public void config(CommandContext context, ParamSet set) {
        context.sendMessage("Config Map:");
        PacketConfig.Type.ALL.getKeys().forEach(type -> {
            Object object = null;
            try {
                object = Cloud.getInstance().getConfig().get(type.getKey());
            }
            catch(InvalidConfigException ex) {
                //
            }

            context.sendMessage("- " + type + " | "
                    + (object == null ? "NULL" : object.toString().replace("\n", "\\n")));
        });
    }

    @Command(label = "end")
    public void end(CommandContext context, ParamSet set) {
        Cloud.getInstance().stop();
    }

    @Command(label = "whitelist")
    public void whitelist(CommandContext context, ParamSet set) {
        List<String> whitelist = Cloud.getInstance().getServer().getWhitelist().getHosts();

        context.sendMessage("Whitelist (" + whitelist.size() + "): "
                + (whitelist.size() == 0 ? "" : "\n\t- " + String.join("\n\t- ", whitelist)));
    }

    @Command(label = "keepalive", flags = "n")
    public void keepalive(CommandContext context, ParamSet set) {
        context.sendMessage("Sending keepalive to all proxy clients ..");
        int count = set.hasFlag("n") ? set.getFlag("n").getInt(0) : 1;

        List<MooClient> clients = Cloud.getInstance().getHub().getProxyClients();
        clients.forEach(mooClient -> {
            for(int i = 0; i < count; i++) {
                PacketMessenger.message(new PacketKeepalive(), mooClient);
            }
        });
    }

    @Command(label = "clients")
    public void clients(CommandContext context, ParamSet set) {
        List<MooClient> clients = Cloud.getInstance().getHub().getAll();
        clients.sort((o1, o2) -> ((Integer) o1.getId()).compareTo(o2.getId()));
        int size = clients.size();

        List<String> l = new ArrayList<>();
        if(!(size > 25)) {
            clients.forEach(nettyClient ->
                    l.add(nettyClient.getName() + ":" + nettyClient.getId()
                            + " [" + nettyClient.getHost() + ":" + nettyClient.getPort() + "] "
                            + "(Type: " + nettyClient.getType().name().toLowerCase() + ")"));
        }

        context.sendMessage("Clients (" + size + "): " + (l.size() == 0 ? (size != 0 ? "Too much to display!" : "") : "\n\t- " + String.join("\n\t- ", l)));
    }

    @Command(label = "players")
    public void players(CommandContext context, ParamSet set) {
        List<MooPlayer> players = new ArrayList<>(Cloud.getInstance().getMooProxy().getPlayers());

        List<String> l = new ArrayList<>();
        if(!(players.size() > 30)) {
            players.forEach(player -> l.add(player.name + "[" + player.uuid + "] @ '" + player.currentServer + "'[#" + player.proxyId + "]"));
        }

        context.sendMessage("Players (" + players.size() + "): " + (l.size() == 0 ? "Nothing to display!" : "\n\t- " + String.join("\n\t- ", l)));
    }

}
