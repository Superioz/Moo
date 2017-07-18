package de.superioz.moo.daemon.task;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.util.Ports;
import lombok.Getter;

@Getter
public class ServerStartTask implements Runnable {

    private String type;
    private int port;
    private boolean autoSave;

    private Server server;

    public ServerStartTask(String type, int port, boolean autoSave) {
        this.type = type;
        this.port = port;
        this.autoSave = autoSave;
    }

    @Override
    public void run() {
        if(port < 0) port = Ports.getAvailablePort();
        this.server = Daemon.getInstance().getServer().startServer(type, Server.DEFAULT_HOST, port, autoSave);
    }

}
