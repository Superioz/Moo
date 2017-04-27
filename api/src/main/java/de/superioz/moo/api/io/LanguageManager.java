package de.superioz.moo.api.io;

import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;

import java.io.File;
import java.util.Hashtable;
import java.util.Locale;
import java.util.function.Function;

/**
 * This manager is for storing messages from a {@link PropertiesConfig}<br>
 * You can also define a formatter which will be called if a string is fetched<br>
 * <p>
 * Placeholder syntax: {n} n := an integer value | {a} a := another property key | %a% a := any char sequence
 */
@Getter
public class LanguageManager {

    private static final long DEFAULT_EXPIRATION = 180;

    @Getter
    private static PropertiesConfig handle;
    private static Function<String, String> formatter;

    private File file;
    private File folder;
    private Locale currentLocale;

    public LanguageManager(File folder, Function<String, String> formatter) {
        this.folder = folder;
        if(!folder.exists()) folder.mkdir();

        LanguageManager.formatter = formatter;
    }

    public LanguageManager(File folder) {
        this(folder, null);
    }

    public LanguageManager(String fileName, File folder, Function<String, String> formatter) {
        this(folder, formatter);
        this.load(fileName);
    }

    public LanguageManager(String fileName, File folder) {
        this(fileName, folder, null);
    }

    /**
     * Checks if the language manager is loaded
     *
     * @return The result
     */
    public boolean isLoaded() {
        return handle.isLoaded();
    }

    /**
     * Loads the languageManager for static access
     *
     * @param fileName The fileName
     */
    public void load(String fileName) {
        if(!fileName.endsWith(".properties")) fileName += ".properties";
        this.file = new File(getFolder(), fileName);

        // get and load properties
        if(handle != null) {
            handle.init(file);
        }
        else {
            handle = new PropertiesConfig(file);
        }
    }

    public void load(Locale locale) {
        this.load(locale.toString());
        this.currentLocale = locale;
    }

    /**
     * Get the elements of this property config
     *
     * @return The elements as string-string map
     */
    public static Hashtable<Object, Object> getElements() {
        return handle;
    }

    /**
     * The size of the properties
     *
     * @return The size as int
     */
    public static int size() {
        return handle.size();
    }

    /**
     * Checks if the properties config contains given key
     *
     * @param key The key
     * @return The result
     */
    public static boolean contains(String key) {
        return handle.containsKey(key);
    }

    /**
     * Retrieves a string from the properties config with given key (use the replacements to replace variables in the text)
     *
     * @param key          The property key
     * @param replacements The variable replacements
     * @return The string
     */
    public static String get(String key, Object... replacements) {
        // if the properties file is not found
        if(handle == null) {
            return "No properties found!";
        }
        String text = (String) handle.get(key, replacements);

        // if the formatter is not null then format the text
        if(formatter != null) {
            text = formatter.apply(text);
        }
        return text;
    }

    /**
     * Formats the text with given replacements
     *
     * @param text         The text to be formatted
     * @param replacements The replacements
     * @return The formatted text
     */
    public static String format(String text, Object... replacements) {
        return StringUtil.format(text, s -> (String) handle.get(s), replacements);
    }

}
