package de.superioz.moo.api.io;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.Getter;
import de.superioz.moo.api.exceptions.InvalidConfigException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

@Getter
public class JsonConfig extends CustomFile {

    private Path path;
    private LinkedHashMap<String, Object> cache = new LinkedHashMap<>();

    public JsonConfig(String filename, String extraPath, Path path) {
        super(filename, extraPath, path, FileType.JSON);
        this.path = path;
    }

    public JsonConfig(String filename, Path path) {
        this(filename, "", path);
    }

    public JsonConfig(String fileName, File path) {
        this(fileName, "", Paths.get(path.getPath()));
    }

    @Override
    public void load(boolean copyDefaults, boolean create) {
        super.load(copyDefaults, create);
        cache = this.parseAll();
    }

    /**
     * Sets the key to given value
     *
     * @param key   The key
     * @param value The value
     * @return The successful
     */
    public boolean set(String key, Object value) {
        String rawKey = key;
        key = "$." + key;

        if(!cache.containsKey(key)) {
            return false;
        }
        cache.put(key, value);
        setRaw(rawKey, value);
        return true;
    }

    public boolean setRaw(String key, Object value) {
        String[] split = key.split("\\.");
        JSONObject root = getAll();
        JSONObject obj = root;

        for(String s : split) {
            assert obj != null;
            Object o = obj.get(s);

            if(o instanceof JSONObject) {
                obj = (JSONObject) o;
            }
            else {
                obj.put(s, value);
            }
        }

        try {

            FileWriter fileWriter = new FileWriter(getFile());
            fileWriter.write(root != null ? root.toString(4) : "");
            fileWriter.flush();
            fileWriter.close();
        }
        catch(IOException e) {
            System.err.println("An error occured:");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Get one key from the json file
     *
     * @param key The key
     * @param <T> The type
     * @return The value
     */
    public <T> T get(String key, T def) {
        try {
            key = "$." + key;

            Object o = cache.get(key);
            if(o instanceof JSONArray) {
                return (T) ((JSONArray) o).toList();
            }
            if(o == null) return def;
            return (T) o;
        }
        catch(Exception e) {
            return def;
        }
    }

    public <T> T get(String key) throws InvalidConfigException {
        T t = get(key, null);
        if(t == null) {
            throw new InvalidConfigException("Couldn't find object with label='" + key + "'!", this);
        }
        return t;
    }

    /**
     * Gets the object from raw key
     *
     * @param key The key
     * @param <T> The type
     * @return The object
     */
    public <T> T getRaw(String key) {
        String[] split = key.split("\\.");
        JSONObject obj = getAll();

        for(String s : split) {
            assert obj != null;
            Object o = obj.get(s);

            if(o instanceof JSONObject) {
                obj = (JSONObject) o;
            }
            else {
                return (T) o;
            }
        }
        return null;
    }

    /**
     * Parses the whole json file
     *
     * @return The map of keys and values
     */
    private LinkedHashMap<String, Object> parseAll() {
        JSONObject root = getAll();
        if(root == null) return new LinkedHashMap<>();

        return new JsonParser(root, "$").getPathList();
    }

    private JSONObject getAll() {
        String s = null;
        try {
            s = Files.toString(getFile(), Charsets.UTF_8);
            return new JSONObject(s);
        }
        catch(IOException e) {
            System.err.println("An error occured:");
            e.printStackTrace();
            return null;
        }
    }

}
