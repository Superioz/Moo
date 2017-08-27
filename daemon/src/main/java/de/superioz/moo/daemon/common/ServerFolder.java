package de.superioz.moo.daemon.common;

import lombok.Getter;
import de.superioz.moo.api.utils.IOUtil;
import de.superioz.moo.api.utils.SystemUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ServerFolder {

    protected File folder;
    protected File startFile;
    protected String name;

    public ServerFolder(File folder, String startFileName) {
        this.folder = folder;

        String fName = folder.getName();
        if(fName.contains("#")) {
            String[] spl = fName.split("#");
            this.name = spl[0];
        }
        else {
            this.name = fName;
        }
        this.startFile = new File(folder, startFileName + "." + (SystemUtil.isWindows() ? "bat" : "sh"));
    }

    public ServerFolder(File folder) {
        this(folder, "start");
    }

    /**
     * Get all serverPatterns inside given folder
     *
     * @param folder        The folder
     * @param startFileName Name of the to-start-server file
     * @return The list of pattern objects
     */
    public static List<ServerFolder> from(File folder, String startFileName) {
        List<ServerFolder> patterns = new ArrayList<>();
        File[] content;
        if(!folder.exists() || (content = folder.listFiles()) == null) return patterns;
        for(File f : content) {
            if(f.isDirectory()) {
                patterns.add(new ServerFolder(f, startFileName));
            }
        }
        return patterns;
    }

    /**
     * Gets a sub file
     *
     * @param name The name
     * @return The file
     */
    public File getSubFile(String name) {
        return new File(folder, name);
    }

    /**
     * Get plugins
     *
     * @return The list of plugins (.jar's)
     */
    public List<File> getPlugins() {
        File pluginsFolder = getSubFile("plugins");
        List<File> l = new ArrayList<>();
        File[] files;
        if(!pluginsFolder.exists() || (files = pluginsFolder.listFiles()) == null) return l;
        for(File f : files) {
            if(f.getName().endsWith(".jar")) l.add(f);
        }
        return l;
    }

    /**
     * Get all worlds from this pattern
     *
     * @return The list of folders which contains a level.dat
     */
    public List<File> getWorlds() {
        List<File> worlds = new ArrayList<>();
        File[] subFiles;
        if((subFiles = folder.listFiles()) == null) return worlds;
        for(File f : subFiles) {
            if(!f.isDirectory()) continue;
            File levelDat = new File(f, "level.dat");
            if(levelDat.exists()) worlds.add(f);
        }
        return worlds;
    }

    /**
     * Copies the server data to another path
     *
     * @param to The file to copy to
     * @return The result
     */
    public boolean copy(int id, File to) {
        String name = getName() + "#" + id;
        if(IOUtil.doesExistsInside(to, name)) return false;

        // copy
        File target = new File(to, name);
        return target.mkdirs() && IOUtil.copyFiles(getFolder(), target);
    }

}
