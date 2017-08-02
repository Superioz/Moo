package de.superioz.moo.daemon;

import de.superioz.moo.api.utils.IOUtil;
import de.superioz.moo.daemon.common.Server;
import de.superioz.moo.daemon.common.ServerPattern;
import de.superioz.moo.daemon.task.RamUsageTask;
import de.superioz.moo.daemon.task.ServerStartQueueTask;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@Getter
public class DaemonInstance {

    private static final String TYPE_COUNT_SPLIT = "#";

    private Map<UUID, Server> startedServerByUuid = new HashMap<>();
    private ExecutorService executors = Executors.newCachedThreadPool();

    private ServerStartQueueTask serverQueue;
    private RamUsageTask ramUsageTask;

    private File patternFolder;
    private File serversFolder;

    private String startFileName;
    private Map<String, ServerPattern> patternByName = new HashMap<>();

    public DaemonInstance(File patternFolder, File serversFolder, String startFileName) {
        this.patternFolder = patternFolder;
        this.serversFolder = serversFolder;
        this.startFileName = startFileName;

        this.serverQueue = new ServerStartQueueTask();
        this.executors.execute(serverQueue);
        this.ramUsageTask = new RamUsageTask(Daemon.getInstance().getConfig().get("ram-usage-delay"));
        this.executors.execute(ramUsageTask);
    }

    /**
     * Creates the important folders
     *
     * @return The result
     */
    public boolean createFolders() {
        boolean r = false;
        try {
            if(!patternFolder.exists()) r = patternFolder.createNewFile();
            if(!serversFolder.exists()) r = serversFolder.createNewFile();
        }
        catch(IOException ex) {
            //
        }
        return r;
    }

    /**
     * Get server with given port
     *
     * @param port The port
     * @return The server object
     */
    public Server getServer(int port) {
        Server server = null;
        for(Server s : startedServerByUuid.values()) {
            if(s.getPort() == port) server = s;
        }
        return server;
    }

    /**
     * Fetches all patterns from given folder
     *
     * @return This
     */
    public DaemonInstance fetchPatterns() {
        this.patternByName.clear();
        ServerPattern.from(patternFolder, startFileName)
                .forEach(serverPattern -> patternByName.put(serverPattern.getName(), serverPattern));
        return this;
    }

    /**
     * Get all available patterns
     *
     * @param cached If false it fetches all patterns before returning the patterns (= live)
     * @return The map of patterns
     */
    public Map<String, ServerPattern> getPatterns(boolean cached) {
        if(!cached) fetchPatterns();
        return patternByName;
    }

    /**
     * Gets a pattern with given name
     *
     * @param type The type
     * @return The serverPattern
     */
    public ServerPattern getPattern(String type, boolean cached) {
        return getPatterns(cached).get(type);
    }

    /**
     * Checks if given pattern exists
     *
     * @param type   The type
     * @param cached Access to cache first?
     * @return The result
     */
    public boolean hasPattern(String type, boolean cached) {
        return getPattern(type, cached) != null;
    }

    /**
     * Cleaned the servers folder
     *
     * @return The result
     */
    public int cleanupServers() throws IOException {
        int cleaned = 0;

        if(!getServersFolder().exists()) return 0;
        File[] files = getServersFolder().listFiles();
        if(files == null) return 0;

        for(File f : files) {
            if(!f.isDirectory()) continue;
            if(IOUtil.deleteFile(f)) cleaned++;
        }
        return cleaned;
    }

    /**
     * Creates a server from given values by getting the ServerPattern and copying the files<br>
     * At this point it might be better to download the pattern from a webserver if you want to
     * have multiple daemon instances with the same pattern, BUT I see it as not important atm.
     *
     * @param type     The type of server (pattern)
     * @param autoSave Auto-save on shutdown?
     * @return The server
     */
    public Server createServer(String type, boolean autoSave) {
        ServerPattern pattern = getPattern(type, false);
        if(pattern == null) {
            Daemon.getInstance().getLogs().info("There is no pattern available with type='" + type + "'!");
            return null;
        }
        File targetFolder = getServersFolder();

        int id = IOUtil.getNextId(targetFolder, TYPE_COUNT_SPLIT, type);
        File target2 = new File(targetFolder, pattern.getName() + TYPE_COUNT_SPLIT + id);

        UUID uuid = UUID.nameUUIDFromBytes((type + ":" + id).getBytes());

        pattern.copy(id, targetFolder);
        return new Server(this, id, uuid, new File(targetFolder, target2.getName()), autoSave);
    }

    /**
     * Starts a server
     *
     * @param type                The type of server (pattern)
     * @param host                The host (e.g. localhost)
     * @param port                The port
     * @param autoSave            Auto-save on shutdown?
     * @param resultOfServerStart The result of the server start
     * @return The (started) server
     */
    public Server startServer(String type, String host, int port, boolean autoSave, Consumer<Server> resultOfServerStart) throws IOException {
        Server server = createServer(type, autoSave);
        if(server == null) return null;
        server.setServerResult(resultOfServerStart);

        // check if the folder is correct and therefore startable
        if(!server.isStartable()) {
            IOUtil.deleteFile(server.getFolder());
            Daemon.getInstance().getLogs().debug("Deleted folder because server is not startable!");
            return null;
        }

        server.start(host, port);
        startedServerByUuid.put(server.getUuid(), server);
        return server;
    }


}
