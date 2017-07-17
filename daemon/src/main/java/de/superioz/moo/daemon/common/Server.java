package de.superioz.moo.daemon.common;

import de.superioz.moo.daemon.Daemon;
import de.superioz.moo.daemon.DaemonInstance;
import de.superioz.moo.daemon.util.ThreadableValue;
import lombok.Getter;
import de.superioz.moo.api.utils.IOUtil;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.exception.MooOutputException;
import de.superioz.moo.protocol.packets.PacketServerAttempt;
import de.superioz.moo.protocol.packets.PacketServerDone;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Getter
public class Server extends ServerPattern {

    public static final int DEFAULT_PORT = 25565;
    public static final String DEFAULT_HOST = "127.0.0.1";

    private Process process;
    private DaemonInstance parent;
    private Console console;
    private boolean autoSave;
    private int id;
    private UUID uuid;

    private int port = DEFAULT_PORT;
    private String host = DEFAULT_HOST;
    private boolean online = false;

    public Server(DaemonInstance parent, int id, UUID uuid, File folder, boolean autoSave) {
        super(folder, parent.getStartFileName());
        this.uuid = uuid;
        this.parent = parent;
        this.id = id;
        this.autoSave = autoSave;
    }

    /**
     * Get the executor service
     *
     * @return The executors object
     */
    public ExecutorService getExecutors() {
        return parent.getExecutors();
    }

    /**
     * Checks if the server is startable
     *
     * @return The result
     */
    public boolean isStartable() {
        return getStartFile().exists() && getFolder().exists();
    }

    /**
     * Starts the server
     *
     * @param host The host
     * @param port The port
     */
    public void start(String host, int port) {
        getExecutors().execute(() -> {
            boolean r = Server.this.run(host, port);

            if(!r) return;
            try {
                Server.this.process.waitFor();
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

            // process is finished
            // server is offline
            online = false;
            getParent().getStartedServerByUuid().remove(getUuid());
            console.close();
            Daemon.logs.info("Server " + getName() + " #" + getId() + " closed.");

            // if autoSave then copy the folder first
            if(isAutoSave()) {
                IOUtil.deleteFile(new File(getParent().getPatternFolder(), getName()));
                IOUtil.copyFiles(getFolder(), new File(getParent().getPatternFolder(), getName()));
            }

            // Delete everything  :(
            IOUtil.deleteFile(getFolder());

            //
            try {
                PacketMessenger.message(new PacketServerDone(PacketServerDone.Type.SHUTDOWN, getUuid(), getName(), getPort()));
            }
            catch(MooOutputException e) {
                //
            }
        });
    }

    public void start(int port) {
        this.start(DEFAULT_HOST, port);
    }

    /**
     * Runs the server task and returns if the server has been started
     * (If the server is already started, the result is false)
     *
     * @param host      The host
     * @param port      The port
     * @return The result
     */
    private boolean run(String host, int port) {
        try {
            PacketMessenger.message(new PacketServerAttempt(PacketServerAttempt.Type.START, getUuid()));
        }
        catch(MooOutputException e) {
            e.printStackTrace();
            return false;
        }

        if(online || !isStartable()) {
            return false;
        }
        this.host = host;
        this.port = port;

        List<String> parameter = new ArrayList<>();
        parameter.add(getStartFile().getAbsolutePath());
        if(port != DEFAULT_PORT) parameter.addAll(Arrays.asList("-p", port + ""));
        if(!host.equals(DEFAULT_HOST)) parameter.addAll(Arrays.asList("-h", host));

        ProcessBuilder builder = new ProcessBuilder(parameter);
        builder.directory(getFolder());

        try {
            this.process = builder.start();
        }
        catch(IOException e) {
            e.printStackTrace();
            return false;
        }

        ThreadableValue<Boolean> done = new ThreadableValue<>(false);
        this.console = new Console(this, this).start(s -> {
            //Daemon.logs.info("INFO(" + getPort() + "): " + s);

            if(!done.get()) {
                if(!s.contains("Done") || !s.contains("For help, type \"help\" or")) {
                    return;
                }
                Daemon.logs.info("Server @" + getHost() + ":" + getPort() + " started.");

                online = true;
                done.set(!done.get());

                //
                try {
                    PacketMessenger.message(new PacketServerDone(PacketServerDone.Type.START, getUuid(), getName(), getPort()));
                }
                catch(MooOutputException e) {
                    //
                }
            }
        }, s -> {
            //Daemon.logs.info("ERROR(" + getPort() + "): " + s);
        });

        return process != null && process.isAlive();
    }

    private boolean run(int port) {
        return run(DEFAULT_HOST, port);
    }

    /**
     * Stops the server
     *
     * @return The result or false if the server is already offline
     */
    public boolean stop() {
        try {
            PacketMessenger.message(new PacketServerAttempt(PacketServerAttempt.Type.SHUTDOWN, getUuid()));
        }
        catch(MooOutputException e) {
            return false;
        }

        if(!online) {
            return false;
        }
        console.write("stop\n");
        return false;
    }

}
