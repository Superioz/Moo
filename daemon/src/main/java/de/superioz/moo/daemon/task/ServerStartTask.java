package de.superioz.moo.daemon.task;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.common.Server;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServerStartTask implements Runnable {

    private String type;
    private int port;
    private boolean autoSave;

    @Override
    public void run() {
        Daemon.getInstance().getServer().startServer(type, Server.DEFAULT_HOST, port, autoSave);
    }

}
