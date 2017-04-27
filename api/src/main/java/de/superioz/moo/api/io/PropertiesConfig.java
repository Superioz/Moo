package de.superioz.moo.api.io;

import com.google.common.base.Charsets;
import lombok.Getter;
import de.superioz.moo.api.utils.StringUtil;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

@Getter
public class PropertiesConfig extends Properties {

    private File file;

    public PropertiesConfig(File file) {
        super();
        this.init(file);
    }

    /**
     * Checks if the file exists
     *
     * @return The result
     */
    public boolean isLoaded() {
        return file.exists();
    }

    /**
     * Gets a property from the config
     *
     * @param key          The key to get the property
     * @param replacements The replacements
     * @return The object (formatted)
     */
    public Object get(Object key, Object... replacements) {
        Object o = super.get(key);
        if(o == null) {
            return "Missing entry '" + key + "'!";
        }
        if(o instanceof String) {
            return StringUtil.format((String) o, s -> (String) get(s), replacements);
        }
        return o;
    }

    @Override
    public Object get(Object key){
        return get(key, new Object[]{});
    }

    /**
     * Initialises the config
     */
    public void init(File file) {
        this.file = file;
        if(!file.exists()) {
            InputStream in = null;

            try {
                in = getClass().getResourceAsStream(file.getName());

                // try class loader
                if(in == null) {
                    in = getClass().getClassLoader().getResourceAsStream(file.getName());
                }
                // still empty? :(
                if(in == null) {
                    return;
                }

                Files.copy(in, file.toPath());
                //System.out.println("language.properties wurde neu erstellt und geladen!");
            }
            catch(Exception e) {
                //
                return;
            }
            finally {
                if(in != null) {
                    try {
                        in.close();
                    }
                    catch(IOException e) {
                        //
                    }
                }
            }
        }

        try(BufferedReader stream = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8))) {
            super.load(stream);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}
