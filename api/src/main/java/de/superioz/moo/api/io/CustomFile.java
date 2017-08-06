package de.superioz.moo.api.io;

import lombok.Getter;

import java.io.*;
import java.nio.file.Path;

@Getter
public class CustomFile {

    /**
     * The name of the file
     */
    protected String filename;

    /**
     * The file itself
     */
    protected File file;

    /**
     * Is the file loaded?
     */
    protected boolean loaded = false;

    /**
     * Filetype to prevent every type and to determine the ending
     */
    protected FileType filetype;

    public CustomFile(String filename, String extraPath, Path root, FileType filetype) {
        this.filetype = filetype;
        this.filename = filename + (filetype == null ? "" : "." + filetype.getName());

        if(!extraPath.isEmpty())
            extraPath = "/" + extraPath + "/";
        file = new File(root + extraPath, this.filename);
    }

    public CustomFile(String filename, Path root, FileType filetype) {
        this(filename, "", root, filetype);
    }

    public CustomFile(String filename, Path root) {
        this(filename, "", root, null);
    }

    /**
     * Takes a file with the same name as this file and copies the content from it
     *
     * @param in   The file
     * @param file This file
     */
    public void copyDefaultsFrom(InputStream in, File file) {
        if(in == null
                || file == null)
            return;

        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;

            while((len = in.read(buffer)) > 0){
                out.write(buffer, 0, len);
            }

            out.close();
            in.close();
        }
        catch(Exception e) {
            System.err.println("An error occured:");
            e.printStackTrace();
        }
    }

    /**
     * Loads this file
     *
     * @param copyDefaults If defaults should be copied into this file
     * @param create       If the file should also be created
     */
    public void load(String resourcePath, boolean copyDefaults, boolean create) {
        if(!file.exists()) {
            file.getParentFile().mkdirs();

            if(copyDefaults) {
                String path = "/";
                if(!resourcePath.isEmpty()) {
                    path += resourcePath + "/";
                }
                InputStream stream = getClass().getResourceAsStream(path + this.filename);
                this.copyDefaultsFrom(stream, file);
                try {
                    stream.close();
                }
                catch(IOException e) {
                    System.err.println("An error occured:");
                    e.printStackTrace();
                }
            }

            // Create
            if(create)
                try {
                    file.createNewFile();
                }
                catch(IOException e) {
                    System.err.println("An error occured:");
                    e.printStackTrace();
                }

            if(file.exists())
                loaded = true;
        }
        else {
            loaded = true;
        }
    }

    public void load(boolean copyDefaults, boolean create) {
        this.load("", copyDefaults, create);
    }

    /**
     * Deletes this file
     *
     * @return If the deletion was successful
     */
    public boolean delete() {
        return this.file.delete();
    }

    public boolean exists() {
        return file.exists();
    }
}
