package de.superioz.moo.spigot.util;

import com.google.common.base.Charsets;
import de.superioz.moo.spigot.Lightning;
import net.md_5.bungee.api.ChatColor;

import java.io.*;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * Created on 18.10.2016.
 */
public class LanguageManager {

    private static Properties properties;
    private String fileName;

    public LanguageManager(String fileName) {
        this.fileName = fileName;
        this.load();
    }

    public static String get(String key, Object... replacements) {
        // TODO add prefix? in its own method?
        String text = properties.getProperty(key);
        if(text == null) {
            // TODO set default properties if not found?
            return "Missing entry '" + key + "' in language.properties!";
        }
        return format(text, replacements);

    }

    public static String format(String string, Object... replacements) {
        return ChatColor.translateAlternateColorCodes('&', MessageFormat.format(string, replacements));
    }

    public void load() {

        File dataFolder = Lightning.getInstance().getDataFolder();
        if(!dataFolder.exists())
            dataFolder.mkdir();

        File file = new File(dataFolder, this.fileName);

        if(!file.exists()) {
            try(InputStream in = Lightning.getInstance().getResource(this.fileName)) {
                Files.copy(in, file.toPath());
                System.out.println("language.properties wurde neu erstellt und geladen!");
            }
            catch(IOException e) {
                e.printStackTrace();
            }

        }
        properties = new Properties();
        try(BufferedReader stream = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8))) {
            properties.load(stream);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        File messagesFile = new File(Lightning.getInstance().getDataFolder().getAbsolutePath() + File.separator + fileName);
        properties = new Properties();
        try(BufferedReader stream = new BufferedReader(new InputStreamReader(new FileInputStream(messagesFile), Charsets.UTF_8))) {
            properties.clear();
            properties.load(stream);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}
