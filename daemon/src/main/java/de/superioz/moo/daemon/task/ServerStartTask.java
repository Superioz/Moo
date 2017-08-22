package de.superioz.moo.daemon.task;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.util.Ports;
import lombok.Getter;

import java.io.IOException;
import java.util.function.Consumer;

@Getter
public class ServerStartTask implements Runnable {

    private String type;
    private int port;
    private String ram;
    private boolean autoSave;

    private Server server;
    private Consumer<Server> resultOfServerStart;

    public ServerStartTask(String type, int port, String ram, boolean autoSave, Consumer<Server> resultOfServerStart) {
        this.type = type;
        this.port = port;
        this.ram = ram;
        this.autoSave = autoSave;
        this.resultOfServerStart = resultOfServerStart;
    }

    @Override
    public void run() {
        if(port < 0) port = Ports.getAvailablePort();
        try {
            this.server = Daemon.getInstance().getServer().startServer(type, Server.DEFAULT_HOST, port, ram, autoSave, resultOfServerStart);
        }
        catch(IOException e) {
            Daemon.getInstance().getLogs().debug("Couldn't start server!", e);
        }
    }

}
