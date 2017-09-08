package de.superioz.moo.api.console.format;

import de.superioz.moo.api.io.LanguageManager;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * This format is for sending complex message formats to a user simply (like lists, ..)
 * To create one, just extend the DisplayFormat.
 */
public abstract class DisplayFormat {

    @Getter
    private Map<String, Boolean> components = new HashMap<>();

    /**
     * In this method you can add components
     */
    public abstract void setupComponents();

    /**
     * Prepares this shit?
     *
     * @return The result
     */
    public boolean prepare() {
        setupComponents();
        return components != null;
    }

    /**
     * Adds a component to the components list to be sent to the player
     *
     * @param message   The component to be added
     * @param condition The condition
     * @return This
     */
    protected DisplayFormat addMessage(String message, boolean condition) {
        components.put(message, condition);
        return this;
    }

    protected DisplayFormat addMessage(String message) {
        return addMessage(message, true);
    }

    /**
     * Gets the message of this key or the key itself (if it's not a key lUl)
     *
     * @param s The key or message
     * @return The message
     */
    protected String getMessage(String s, Object... replacements) {
        if(LanguageManager.contains(s)) return LanguageManager.get(s, replacements);
        if(s.isEmpty()) return "";
        return s;
    }

}
