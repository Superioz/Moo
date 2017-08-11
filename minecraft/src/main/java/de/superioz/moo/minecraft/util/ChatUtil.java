package de.superioz.moo.minecraft.util;

import de.superioz.moo.api.util.SpecialCharacter;
import de.superioz.moo.minecraft.chat.MessageComponent;
import de.superioz.moo.minecraft.chat.MessageEventable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

public class ChatUtil {

    public static final ChatColor[] GRADIENT_BLUE = {ChatColor.BLUE, ChatColor.DARK_AQUA, ChatColor.AQUA};
    public static final ChatColor[] GRADIENT_RED = {ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW};

    /**
     * Gets an event message from the component by applying the events to the component
     *
     * @param component  The component
     * @param eventables The eventable objects (to define the events)
     * @return The message component
     */
    public static MessageComponent getEventMessage(MessageComponent component, MessageEventable... eventables) {
        if(component == null) return component;

        // replace events
        // either for one eventable (format all events with one pair of values)
        // or the eventables after one another
        if(eventables.length == 0) {
            component.formatAll();
        }
        else {
            for(MessageEventable eventable : eventables) {
                ClickEvent.Action clickAction = eventable.getClickAction();
                HoverEvent.Action hoverAction = eventable.getHoverAction();
                boolean condition = eventable.isCondition();

                component.formatConditioned(hoverAction, clickAction, condition);
            }
        }
        return component;
    }

    public static MessageComponent getEventMessage(MessageComponent component, ClickEvent.Action clickAction, boolean condition) {
        return getEventMessage(component, new MessageEventable(null, clickAction, condition));
    }

    public static MessageComponent getEventMessage(MessageComponent component, ClickEvent.Action clickAction) {
        return getEventMessage(component, new MessageEventable(null, clickAction));
    }

    public static MessageComponent getEventMessage(MessageComponent component) {
        return getEventMessage(component, (ClickEvent.Action) null);
    }

    /**
     * Similar to {@link #getEventMessage(MessageComponent, MessageEventable...)}
     * but with a string message instead of a message component
     *
     * @param message    The message
     * @param eventables The eventables to format the component
     */
    public static MessageComponent getEventMessage(String message, MessageEventable... eventables) {
        return getEventMessage(new MessageComponent(message), eventables);
    }

    public static MessageComponent getEventMessage(String message, ClickEvent.Action clickAction, boolean condition) {
        return getEventMessage(message, new MessageEventable(null, clickAction, condition));
    }

    public static MessageComponent getEventMessage(String message, ClickEvent.Action clickAction) {
        return getEventMessage(message, new MessageEventable(null, clickAction));
    }

    public static MessageComponent getEventMessage(String message, boolean condition) {
        return getEventMessage(message, null, condition);
    }

    public static MessageComponent getEventMessage(String message) {
        return getEventMessage(message, true);
    }

    /**
     * Colors given string
     *
     * @param s The string
     * @return The colored string
     */
    public static String colored(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Colors all given strings
     *
     * @param strings The strings
     * @return The colored strings
     */
    public static String[] colored(String... strings) {
        String[] colored = new String[strings.length];

        for(int i = 0; i < colored.length; i++) {
            colored[i] = colored(strings[i]);
        }

        return colored;
    }

    /**
     * Fabulize the string
     *
     * @param s The string
     * @return The result
     */
    public static String fabulize(String s) {
        return SpecialCharacter.apply(colored(s));
    }

    /**
     * Fabulize the string array
     *
     * @param strings The array
     * @return The string
     */
    public static String[] fabulize(String... strings) {
        String[] colored = new String[strings.length];

        for(int i = 0; i < colored.length; i++) {
            colored[i] = colored(strings[i]);
        }

        return colored;
    }

}
