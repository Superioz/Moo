package de.superioz.moo.daemon.commands;

import com.google.common.io.Files;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.common.ServerPattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 18.11.2016.
 */
public class MainCommand {

    @Command(label = "pattern")
    public void pattern(CommandContext context, ParamSet args) {
        Daemon.getInstance().getLogs().info("Patterns (" + Daemon.getInstance().getServer().getPatternByName().size() + "):");
        for(ServerPattern pattern : Daemon.getInstance().getServer().getPatternByName().values()) {
            List<String> plugins = new ArrayList<>();
            pattern.getPlugins().forEach(file -> plugins.add(Files.getNameWithoutExtension(file.getPath())));
            List<String> worlds = new ArrayList<>();
            pattern.getWorlds().forEach(file -> worlds.add(file.getName()));

            Daemon.getInstance().getLogs().info("- " + pattern.getName() + " " +
                    "(Plugins: " + StringUtil.join(", ", plugins) + ")" +
                    "(Worlds: " + StringUtil.join(", ", worlds));
        }
    }

    @Command(label = "start", usage = "<serverType> [amount]")
    public void start(CommandContext context, ParamSet args) {
        // first check if the daemon is connected
        if(!Daemon.getInstance().isConnected()) {
            context.sendMessage("&cYou need to be connected to the cloud for that!");
            return;
        }

        // if no arguments
        if(args.size() == 0) {
            Daemon.getInstance().getLogs().info("Usage: start <serverType>");

            List<String> types = new ArrayList<>();
            Daemon.getInstance().getServer().getPatternByName().forEach((s, serverPattern) -> types.add(s));
            Daemon.getInstance().getLogs().info("Types: {" + StringUtil.join(", ", types) + "}");
            return;
        }
        String name = args.get(0);
        int amount = args.getInt(1, 1);

        Daemon.getInstance().getLogs().info("Starting server .. (" + amount + "x " + name + ")");
        Daemon.getInstance().startServer(name, "", false, amount, null);
    }

    @Command(label = "stop")
    public void stop(CommandContext context, ParamSet args) {
        Daemon.getInstance().getLogs().info("Stopping every server ..");
        Daemon.getInstance().closeEveryServer(server
                -> System.out.println("- Stopping " + server.getName() + "#" + server.getId() + " .."));
    }

    @Command(label = "server")
    public void server(CommandContext context, ParamSet args) {
        // first check if the daemon is connected
        if(!Daemon.getInstance().isConnected()) {
            context.sendMessage("&cYou need to be connected to the cloud for that!");
            return;
        }

        Daemon.getInstance().getLogs().info("Server (" + Daemon.getInstance().getServer().getStartedServerByUuid().size() + "):");

        for(Server server : Daemon.getInstance().getServer().getStartedServerByUuid().values()) {
            context.sendMessage("- Server: " + server.getName() + " (#" + server.getId() + "), " + server.getHost() + ":" + server.getPort());
        }
    }

    @Command(label = "end")
    public void end(CommandContext context, ParamSet args) {
        if(Daemon.getInstance().getServer().getStartedServerByUuid().size() > 0) {
            Daemon.getInstance().getLogs().warning("You need to stop the server before ending the program! Just type 'stop'.");
            return;
        }

        Moo.getInstance().disconnect();
        Daemon.getInstance().getLogs().info("Shutdown processes ..");
        Daemon.getInstance().getServer().getExecutors().shutdown();

        System.exit(0);
    }

    @Command(label = "clear")
    public void clear(CommandContext context, ParamSet args) {
        // clears the folder
        try {
            context.sendMessage("Cleanup ..");
            Daemon.getInstance().getServer().cleanupServers();
        }
        catch(IOException e) {
            context.sendMessage("Couldn't cleanup!");
        }
    }

}
