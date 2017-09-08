package de.superioz.moo.api.utils;

import com.google.common.base.Strings;
import de.superioz.moo.api.collection.PageableList;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.Procedure;

import java.util.List;
import java.util.function.Consumer;

/**
 * This class holds methods for displaying data in chat or in console
 */
public final class DisplayFormats {

    /**
     * Get the list seperation (e.g.: ======[ ... ]======)
     *
     * @param repeatChar      The repeatchar (here '=')
     * @param repeat          The amount of repeation (here 6)
     * @param spacerCharLeft  The spacerCharLeft (here '[')
     * @param spacerCharRight The spacerCharRight (here ']')
     * @param header          The header (here '...')
     * @return The seperation as string
     */
    public static String getListSeperation(String repeatChar, int repeat,
                                           String spacerCharLeft, String spacerCharRight, String header) {
        String first = Strings.repeat(repeatChar, repeat);
        return first + spacerCharLeft + " " + header + " " + spacerCharRight + first;
    }

    /**
     * Returns the string as singular or plural depending on the given size<br>
     * E.g.: minute and minutes
     *
     * @param i        The size
     * @param singular The singular form (i = 1)
     * @param plural   The plural form (i > 1)
     * @return The string
     */
    public static String getPluralOrSingular(int i, String singular, String plural) {
        return i > 1 ? plural : singular;
    }

    /**
     * Represents a list sending process
     *
     * @param seperation    Sends the seperation
     * @param entries       The entries of the list
     * @param entry         Sends an entry
     * @param footerMessage Sends the footer message
     */
    public static <T> void sendPageableList(Procedure seperation, List<T> entries,
                                            Consumer<T> entry, Procedure footerMessage) {
        seperation.invoke();
        for(T t : entries) {
            entry.accept(t);
        }
        footerMessage.invoke();
        seperation.invoke();
    }

    /**
     * Represents a list sending process but with setting directly all values for the format<br>
     * Only for commands
     *
     * @param context       The command context (for message sending)
     * @param list          The list
     * @param page          The page of the list
     * @param sepFormat     The seperation format (e.g. =====[ .. ]=====)
     * @param header        The header
     * @param emptyEntry    If an entry is empty send this
     * @param entry         If an entry is not empty
     * @param footerMessage The message on the footer if there are more than one page
     * @param <T>           The type of entries
     */
    public static <T> void sendPageableList(CommandContext context, PageableList<T> list, int page, String emptyList, String sepFormat,
                                            String header, String emptyEntry, Consumer<T> entry, Procedure footerMessage) {
        if(list.size() == 0) {
            context.sendMessage(emptyList);
            return;
        }
        if(page < 0 || page > list.getMaxPages()) return;

        sendPageableList(() -> context.sendMessage(StringUtil.format(sepFormat, header)), list.getPage(page), t -> {
            if(t == null) {
                context.sendMessage(emptyEntry);
                return;
            }
            entry.accept(t);
        }, () -> {
            if(page < list.getMaxPages() && footerMessage != null) {
                context.sendMessage("");
                footerMessage.invoke();
            }
        });
    }

    /**
     * Similar to {@link #sendPageableList(CommandContext, PageableList, int, String, String, String, String, Consumer, Procedure)}
     * but without seperation format
     *
     * @param context       The command context (for message sending)
     * @param list          The list
     * @param page          The page of the list
     * @param header        The header
     * @param emptyEntry    If an entry is empty send this
     * @param entry         If an entry is not empty
     * @param footerMessage The message on the footer if there are more than one page
     * @param <T>           The type of entries
     */
    public static <T> void sendPageableList(CommandContext context, PageableList<T> list, int page,
                                            String emptyList, String header, String emptyEntry, Consumer<T> entry, Procedure footerMessage) {
        sendPageableList(context, list, page, emptyList, LanguageManager.get("list-format-seperation",
                header + " &7(&e" + (page + 1) + "&7/" + (list.getMaxPages() + 1) + ")"),
                header, emptyEntry, entry,
                footerMessage);
    }

}
