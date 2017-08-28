package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.common.MooServer;
import de.superioz.moo.api.database.objects.PlayerData;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.cloud.Cloud;
import de.superioz.moo.netty.common.PacketMessenger;
import de.superioz.moo.netty.packets.PacketKeepalive;
import de.superioz.moo.netty.server.MooClient;

import java.util.*;

public class CloudCommand implements EventListener {

    /**
     * Stops the cloud?
     *
     * @param context .
     * @param set     .
     */
    @Command(label = "end")
    public void end(CommandContext context, ParamSet set) {
        try {
            Cloud.getInstance().stop();
        }
        catch(Exception e) {
            System.exit(0);
        }
    }

    /**
     * Displays every ip address on the whitelist
     *
     * @param context .
     * @param set     .
     */
    @Command(label = "whitelist")
    public void whitelist(CommandContext context, ParamSet set) {
        List<String> whitelist = Cloud.getInstance().getServer().getWhitelist().getHosts();

        context.sendMessage("Whitelist (" + whitelist.size() + "): "
                + (whitelist.size() == 0 ? "" : "\n\t- " + String.join("\n\t- ", whitelist)));
    }

    /**
     * Sends a keepalive packet to every client
     *
     * @param context .
     * @param set     .
     */
    @Command(label = "keepalive", flags = "n")
    public void keepalive(CommandContext context, ParamSet set) {
        context.sendMessage("Sending keepalive to all proxy clients ..");
        int count = set.hasFlag("n") ? set.getFlag("n").getInt(0) : 1;

        List<MooClient> clients = Cloud.getInstance().getClientManager().getProxyClients();
        clients.forEach(mooClient -> {
            for(int i = 0; i < count; i++) {
                PacketMessenger.message(new PacketKeepalive(), mooClient);
            }
        });
    }

    /**
     * Displays every client currently connected to the cloud
     *
     * @param context .
     * @param set     .
     */
    @Command(label = "clients")
    public void clients(CommandContext context, ParamSet set) {
        List<MooClient> clients = Cloud.getInstance().getClientManager().getAll();
        clients.sort(Comparator.comparingInt(MooClient::getId));
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

    /**
     * Displays every spigot server/lightning server currently connected (with motd, players, ..)
     *
     * @param context .
     * @param set     .
     */
    @Command(label = "lightnings")
    public void lightnings(CommandContext context, ParamSet set) {
        Map<UUID, MooServer> servers = Cloud.getInstance().getMooProxy().getSpigotServers();

        List<String> l = new ArrayList<>();
        if(!(servers.size() > 30)) {
            servers.forEach((uuid, mooServer)
                    -> l.add(mooServer.getType() + "[" + uuid + "]: "
                    + mooServer.getOnlinePlayers() + "/" + mooServer.getMaxPlayers()
                    + " ('" + mooServer.getMotd() + "')"));
        }

        context.sendMessage("Lightnings (" + l.size() + "): " + (l.size() == 0 ? "Nothing to display!" : "\n\t- " + String.join("\n\t- ", l)));
    }

    /**
     * Displays every player online on this network (could be too much output, so only 30 at a time sorry)
     *
     * @param context .
     * @param set     .
     */
    @Command(label = "players")
    public void players(CommandContext context, ParamSet set) {
        List<PlayerData> players = new ArrayList<>(Cloud.getInstance().getMooProxy().getPlayers());

        List<String> l = new ArrayList<>();
        if(!(players.size() > 30)) {
            players.forEach(player -> l.add(player.getLastName() + "[" + player.getUuid() + "] @ '"
                    + player.getCurrentServer() + "'[#" + player.getCurrentProxy() + "]"));
        }

        context.sendMessage("Players (" + players.size() + "): " + (l.size() == 0 ? "Nothing to display!" : "\n\t- " + String.join("\n\t- ", l)));
    }

}
