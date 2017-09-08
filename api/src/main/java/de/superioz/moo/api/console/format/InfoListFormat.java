package de.superioz.moo.api.console.format;

import de.superioz.moo.api.utils.StringUtil;

public class InfoListFormat extends DisplayFormat {

    private String entryFormat;
    private String header;

    public InfoListFormat() {
    }

    @Override
    public void setupComponents() {
        if(header != null) {
            addMessage(header);
        }
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

    public InfoListFormat entryFormat(String entryFormat) {
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
     * @param condition    The condition to be true to have hover/click events
     * @param replacements The replacements (for the languagemanager)
     * @return This
     */
    public InfoListFormat entryc(String entry, boolean condition, Object... replacements) {
        String message = getMessage(entry, replacements);
        String fullMessage = message.isEmpty()
                ? StringUtil.format(entryFormat, replacements)
                : StringUtil.format(entryFormat != null ? entryFormat : "{0}", message);

        addMessage(fullMessage, condition);
        return this;
    }

    public InfoListFormat entry(String entry, Object... replacements) {
        return entryc(entry, true, replacements);
    }

    public InfoListFormat entryr(Object... replacements) {
        return entryc("", true, replacements);
    }

}
