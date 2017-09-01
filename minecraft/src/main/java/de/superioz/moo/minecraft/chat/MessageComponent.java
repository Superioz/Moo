package de.superioz.moo.minecraft.chat;

import de.superioz.moo.api.util.Validation;
import de.superioz.moo.minecraft.util.ChatUtil;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.api.utils.EnumUtil;
import de.superioz.moo.api.utils.StringUtil;
import net.md_5.bungee.api.chat.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A message component which uses a specific syntax to automatically determine the click/hover events<br>
 * Format: '${"replacement",[0-9]"hoverText",[0-9]"clickComponent"}'<br>
 * The number before the content (either hover or click) is optional and defines the action
 *
 * @see TextComponent
 * @see ClickEvent.Action
 * @see HoverEvent.Action
 */
@Getter
public class MessageComponent {

    public static final Pattern SYNTAX_PATTERN_PART = Pattern.compile("[0-9]?\"[^\"]*\",?");
    public static final Pattern EVENT_SYNTAX_PATTERN = Pattern.compile("\\$\\{(" + SYNTAX_PATTERN_PART.pattern() + ",?){3}}\\$");

    //private TextComponent textComponent;
    private String message;
    private List<Pair<String, TextEntry>> entryList = new ArrayList<>();
    private int entryCursor = 0;
    private boolean formatted = false;

    /**
     * Returns a valid syntax for an {@link EventEntry}
     *
     * @param m   The original message to be displayed
     * @param hId The id of the hoverEvent action {@link HoverEvent.Action#ordinal()}
     * @param e1  The content of the hoverEvent
     * @param cId The id of the clickEvent action {@link ClickEvent.Action#ordinal()}
     * @param e2  The content of the clickEvent
     * @return The syntax as string
     */
    public static String event(String m, Integer hId, String e1, Integer cId, String e2) {
        return "${\"" + m + "\","
                + (hId != null ? hId : "") + "\"" + e1 + "\","
                + (cId != null ? cId : "") + "\"" + e2 + "\"}$";
    }

    public MessageComponent(String message) {
        this.message = message;

        for(String s : StringUtil.split(message, EVENT_SYNTAX_PATTERN.pattern(),
                true)) {
            if(EVENT_SYNTAX_PATTERN.matcher(s).matches()) {
                this.entryList.add(new Pair<>(s, new EventEntry(s)));
            }
            else {
                this.entryList.add(new Pair<>(s, new TextEntry(s)));
            }
        }
    }

    /**
     * Just a helper method to be not bound to use {@link StringUtil#format}, instead you can just use this method (shorter!)
     *
     * @param format       The format to be formatted
     * @param replacements The replacements (for events use {@link #event(String, Integer, String, Integer, String)})
     * @return A new component instance
     */
    public static MessageComponent formatted(String format, Object... replacements) {
        return new MessageComponent(StringUtil.format(format, replacements));
    }

    /**
     * Replaces given string with given {@link TextEntry}
     *
     * @param toReplace The string to be replaced (e.g. "%diamond")
     * @param entry     The entry to replace given string (e.g. a TextEntry with a {@link TranslatableComponent})
     * @return This
     */
    public MessageComponent replace(String toReplace, TextEntry entry) {
        for(int i = 0; i < entryList.size(); i++) {
            Pair<String, TextEntry> entryPair = entryList.get(i);
            String raw = entryPair.getKey();
            if(!raw.contains(toReplace)) continue;

            String[] spl = raw.split(toReplace);

            entryList.remove(i);

            if(spl.length > 1) {
                entryList.add(i, new Pair<>(spl[1], new TextEntry(spl[1])));
            }
            entryList.add(i, new Pair<>(toReplace, entry));
            entryList.add(i, new Pair<>(spl[0], new TextEntry(spl[0])));
            break;
        }
        return this;
    }

    public MessageComponent replace(String toReplace, BaseComponent component) {
        return replace(toReplace, new TextEntry(component));
    }

    /**
     * Formats an event syntax with given events and replacements
     *
     * @param hoverAction  The action for an {@link HoverEvent}
     * @param clickAction  The action for an {@link ClickEvent}
     * @param condition    false = no events; true = events
     * @param replacements The replacements
     * @return This
     */
    public MessageComponent formatConditioned(HoverEvent.Action hoverAction, ClickEvent.Action clickAction, boolean condition, Object... replacements) {
        EventEntry eventEntry = null;
        for(int i = entryCursor; i < entryList.size(); i++) {
            Pair<String, TextEntry> entry = entryList.get(i);
            TextEntry value = entry.getValue();

            if(value instanceof EventEntry) {
                this.entryCursor = i;
                eventEntry = (EventEntry) value;
            }
        }
        if(eventEntry == null) return this;
        eventEntry = eventEntry.initSyntax(StringUtil.format(eventEntry.getHandle(), replacements));

        // set events if the condition is true
        if(condition) {
            eventEntry.initEvents(hoverAction, clickAction);
        }
        formatted = true;
        return this;
    }

    public MessageComponent format(HoverEvent.Action hoverAction, ClickEvent.Action clickAction,
                                   Object... replacements) {
        return formatConditioned(hoverAction, clickAction, true, replacements);
    }

    public MessageComponent format(boolean condition, Object... replacements) {
        return formatConditioned(null, null, condition, replacements);
    }

    /**
     * Formats all to-format parts but without replacements, only the given events
     *
     * @param hoverAction The hoveraction
     * @param clickAction The clickaction
     * @return This
     */
    public MessageComponent formatAll(HoverEvent.Action hoverAction, ClickEvent.Action clickAction) {
        for(Pair<String, TextEntry> entryPair : entryList) {
            TextEntry textEntry = entryPair.getValue();

            // if the text entry is an evententry format it with actions
            if(textEntry instanceof EventEntry) {
                format(hoverAction, clickAction);
            }
        }
        return this;
    }

    /**
     * Formats all events by automatically get the click and hover event action (only if the user
     * didn't set the action himself we will use the default ones)
     *
     * @return This
     */
    public MessageComponent formatAll() {
        return formatAll(HoverEvent.Action.SHOW_TEXT, ClickEvent.Action.SUGGEST_COMMAND);
    }

    /**
     * Get the size of the founded events
     *
     * @return The size as int
     */
    public int getEventSize() {
        int i = 0;

        for(Pair<String, TextEntry> entry : entryList) {
            if(entry.getValue() instanceof EventEntry) i++;
        }
        return i;
    }

    /**
     * Turns {@link #entryList} into a {@link TextComponent}
     *
     * @return The component
     */
    public TextComponent toTextComponent() {
        if(!formatted) {
            formatAll();
        }
        TextComponent component = new TextComponent();

        entryList.forEach(entryPair -> component.addExtra(entryPair.getValue().toComponent()));
        return component;
    }

    /**
     * An entry of the message component. This is a wrapper class for a {@link BaseComponent}
     */
    public static class TextEntry {

        @Getter
        private String handle;
        @Getter
        private BaseComponent component;

        public TextEntry(String handle) {
            this.handle = handle;
        }

        public TextEntry(BaseComponent component) {
            this.handle = "";
            this.component = component;
        }

        public BaseComponent toComponent() {
            if(component != null) return component;
            return new TextComponent(TextComponent.fromLegacyText(ChatUtil.fabulize(getHandle())));
        }

    }

    /**
     * Similar to {@link TextEntry} but with saving events to
     */
    @Getter
    public static class EventEntry extends TextEntry {

        private static final HoverEvent.Action DEFAULT_HOVER_ACTION = HoverEvent.Action.SHOW_TEXT;
        private static final ClickEvent.Action DEFAULT_CLICK_ACTION = ClickEvent.Action.SUGGEST_COMMAND;

        private List<String> syntaxParts;

        private String message = "";
        private String hoverEventContent = "";
        private String clickEventContent = "";

        @Setter private HoverEvent hoverEvent;
        @Setter private ClickEvent clickEvent;

        public EventEntry(String syntax) {
            super(syntax);
            this.initSyntax(syntax);
        }

        /**
         * Initialises the syntax by splitting the whole syntax into multiple parts
         *
         * @param syntax The syntax
         * @return This
         */
        public EventEntry initSyntax(String syntax) {
            this.syntaxParts = StringUtil.find(MessageComponent.SYNTAX_PATTERN_PART, syntax);

            if(syntaxParts.size() >= 1) {
                this.message = syntaxParts.get(0);

                // format message
                if(!message.startsWith("\"")) message = message.substring(1, message.length());
                message = message.substring(1, message.length() - 2);
            }
            if(syntaxParts.size() >= 2) {
                this.hoverEventContent = syntaxParts.get(1);
                hoverEventContent = hoverEventContent.substring(0, hoverEventContent.length() - 1);
            }
            if(syntaxParts.size() >= 3) {
                this.clickEventContent = syntaxParts.get(2);
            }
            return this;
        }

        /**
         * Initialise the events by setting the content and given action (or automatically fetching the action)
         *
         * @param hoverAction The hoverAction (can be null)
         * @param clickAction The clickAction (can be null)
         * @return This
         */
        public EventEntry initEvents(HoverEvent.Action hoverAction, ClickEvent.Action clickAction) {
            if(hoverEvent == null && !hoverEventContent.isEmpty()) {
                // user placed a number in front of the content (=action)
                if(!hoverEventContent.startsWith("\"")) {
                    String[] split = hoverEventContent.split("[\"]");
                    String before = split[0];
                    hoverEventContent = split[1];

                    // check if id otherwise name
                    if(Validation.INTEGER.matches(before)) {
                        int id = Integer.parseInt(before);
                        hoverAction = (HoverEvent.Action) EnumUtil.getEnumById(HoverEvent.Action.class, id);
                    }
                    else {
                        hoverAction = (HoverEvent.Action) EnumUtil.getEnumByName(HoverEvent.Action.class, before);
                    }
                }
                hoverEventContent = hoverEventContent.replaceAll("[\"]", "");

                // if action is null set default
                if(hoverAction == null) hoverAction = DEFAULT_HOVER_ACTION;

                // set event
                this.hoverEvent = new HoverEvent(hoverAction,
                        TextComponent.fromLegacyText(ChatUtil.fabulize(hoverEventContent)));
            }
            if(clickEvent == null && !clickEventContent.isEmpty()) {
                // user placed a number in front of the content (=action)
                if(!clickEventContent.startsWith("\"")) {
                    String[] split = clickEventContent.split("[\"]");
                    String before = split[0];
                    clickEventContent = split[1];

                    // check if id otherwise name
                    if(Validation.INTEGER.matches(before)) {
                        int id = Integer.parseInt(before);
                        clickAction = (ClickEvent.Action) EnumUtil.getEnumById(ClickEvent.Action.class, id);
                    }
                    else {
                        clickAction = (ClickEvent.Action) EnumUtil.getEnumByName(ClickEvent.Action.class, before);
                    }
                }
                clickEventContent = clickEventContent.replaceAll("[\"]", "");

                // if action is null set default
                if(clickAction == null) clickAction = DEFAULT_CLICK_ACTION;

                // set event
                this.clickEvent = new ClickEvent(clickAction, clickEventContent);
            }
            return this;
        }

        public EventEntry initEvents(ClickEvent.Action clickAction) {
            return initEvents(null, clickAction);
        }

        public EventEntry initEvents() {
            return initEvents(null, null);
        }

        @Override
        public BaseComponent toComponent() {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(ChatUtil.fabulize(message)));
            if(hoverEvent != null) {
                component.setHoverEvent(hoverEvent);
            }
            if(clickEvent != null) {
                component.setClickEvent(clickEvent);
            }
            return component;
        }

    }

}
