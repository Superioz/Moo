package de.superioz.moo.minecraft.chat.formats;

import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.minecraft.chat.MessageComponent;
import lombok.Getter;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class InfoListFormat extends DisplayFormat {

    @Getter
    private List<TextComponent> entries = new ArrayList<>();

    private String entryFormat;
    private String header;

    public InfoListFormat() {

    }

    @Override
    public void setupComponents() {
        if(header != null) {
            addComponent(header);
        }

        entries.forEach(component -> {
            if(component == null) return;
            addComponent(component);
        });
    }

    /*
    ===================
    SETTER
    ===================
     */

    public InfoListFormat header(String header, Object... replacements) {
        this.header = getMessage(header, replacements);
        return this;
    }

    public InfoListFormat entry(String entryFormat) {
        this.entryFormat = getMessage(entryFormat);
        return this;
    }

    /*
    ===================
    OTHERS
    ===================
     */

    /**
     * Adds an entry to the list by converting the message into a TextComponent. Either use the key of a message
     * from the language properties or use a message itself.
     *
     * @param entry        The entry (either key or message)
     * @param replacements The replacements (for the languagemanager)
     * @return This
     */
    public InfoListFormat entryc(String entry, boolean condition, Object... replacements) {
        String message = getMessage(entry, replacements);
        String fullMessage = StringUtil.format(entryFormat != null ? entryFormat : "{0}", message);

        entries.add(new MessageComponent(fullMessage).format(condition).toTextComponent());
        return this;
    }

    public InfoListFormat entry(String entry, Object... replacements) {
        return entryc(entry, true, replacements);
    }

}
