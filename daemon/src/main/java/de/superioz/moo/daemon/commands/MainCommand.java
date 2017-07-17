package de.superioz.moo.daemon.commands;

import com.google.common.io.Files;
import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.common.ServerPattern;
import de.superioz.moo.daemon.util.Ports;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.daemon.common.Server;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 18.11.2016.
 */
public class MainCommand {

    @Command(label = "pattern")
    public void pattern(CommandContext context, ParamSet args) {
        Daemon.logs.info("Patterns (" + Daemon.server.getPatternByName().size() + "):");
        for(ServerPattern pattern : Daemon.server.getPatternByName().values()) {
            List<String> plugins = new ArrayList<>();
            pattern.getPlugins().forEach(file -> plugins.add(Files.getNameWithoutExtension(file.getPath())));
            List<String> worlds = new ArrayList<>();
            pattern.getWorlds().forEach(file -> worlds.add(file.getName()));

            Daemon.logs.info("- " + pattern.getName() + " " +
                    "(Plugins: " + StringUtil.join(", ", plugins) + ")" +
                    "(Worlds: " + StringUtil.join(", ", worlds));
        }
    }

    @Command(label = "start")
    public void start(CommandContext context, ParamSet args) {
        if(args.size() == 0){
            Daemon.logs.info("Usage: start <serverType>");

            List<String> types = new ArrayList<>();
            Daemon.server.getPatternByName().forEach((s, serverPattern) -> types.add(s));
            Daemon.logs.info("Types: {" + StringUtil.join(", ", types) +  "}");
            return;
        }
        String name = args.get(0);

        Daemon.logs.info("Starting server dynamically ..");
        Server server = Daemon.server.startServer(name, "127.0.0.1", Ports.getAvailablePort(), true);
        if(server == null) {
            Daemon.logs.info("Couldn't start server!");
        }
    }

    @Command(label = "stop")
    public void stop(CommandContext context, ParamSet args) {
        Daemon.logs.info("Stopping server ..");
        for(Server server : Daemon.server.getStartedServerByUuid().values()) {
            System.out.println("- Stopping " + server.getName() + "#" + server.getId() + " ..");
            server.stop();
        }
    }

    @Command(label = "server")
    public void server(CommandContext context, ParamSet args){
        Daemon.logs.info("Server (" + Daemon.server.getStartedServerByUuid().size() + "):");

        for(Server server : Daemon.server.getStartedServerByUuid().values()){
            Daemon.logs.info("- Server: " + server.getName() + " (#" + server.getId() + "), " + server.getHost() + ":" + server.getPort());
        }
    }

}
