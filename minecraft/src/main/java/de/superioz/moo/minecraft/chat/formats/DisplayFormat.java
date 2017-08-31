package de.superioz.moo.minecraft.chat.formats;

import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.minecraft.chat.MessageComponent;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * This format is for sending complex message formats to a user simply (like lists, ..)
 * To create one, just extend the DisplayFormat.
 */
public abstract class DisplayFormat {

    @Getter
    private List<TextComponent> components = new ArrayList<>();

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
     * @param component The component to be added
     * @return This
     */
    protected DisplayFormat addComponent(TextComponent component) {
        components.add(component);
        return this;
    }

    protected DisplayFormat addComponent(String message) {
        components.add(new MessageComponent(message).toTextComponent());
        return this;
    }

    /**
     * Gets the message of this key or the key itself (if it's not a key lUl)
     *
     * @param s The key or message
     * @return The message
     */
    protected String getMessage(String s, Object... replacements) {
        if(LanguageManager.contains(s)) return LanguageManager.get(s, replacements);
        return s;
    }

}
