package de.superioz.moo.api.util;

import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;
import de.superioz.moo.api.io.LanguageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an addition to the class because this
 * message sender either formats the given format and adds a {@link Procedure} which will send
 * the given message or it directly adds a given procedure.<br>
 * After finishing using {@link #execute()} will invoke all procedures
 * <br><br>
 * Example usage:<br>
 * You want to send a group information to the player and you decide to make the permissions
 * entry hoverable. <br>So simply use: {@link #addTranslated(String, Object...)} or {@link #add(Object...)} for the simple
 * entries and {@link #add(Procedure)} to send the hoverable message (mostly CommandContext#sendEventMessage)
 *
 * @see Procedure
 */
public abstract class MessageFormatSender {

    @Getter
    private String format;
    private List<Procedure> messageSender = new ArrayList<>();

    public MessageFormatSender(String format) {
        this.format = format;
    }

    /**
     * Just sends a message (for example with {@link CommandContext})
     *
     * @param s The message to be sent
     */
    public abstract void sendMessage(String s);

    /**
     * Adds a translated string to be sent
     *
     * @param property     The property key
     * @param replacements The replacements
     * @return This
     */
    public MessageFormatSender addTranslated(String property, Object... replacements) {
        return add(LanguageManager.get(property, replacements));
    }

    /**
     * Adds a new message where the {@code format} will be formatted with given objects
     *
     * @param replacements The replacements
     * @return This
     * @see StringUtil#format(String, Object...)
     * @see #add(Procedure)
     */
    public MessageFormatSender add(Object... replacements) {
        return add(() -> sendMessage(StringUtil.format(format, replacements)));
    }

    /**
     * Directly adds a procedure to the procedure list instead of formatting
     * a message beforehand. This procedure must send a message in any way otherway
     * it wouldn't make sense
     *
     * @param procedure The procedure
     * @return This
     */
    public MessageFormatSender add(Procedure procedure) {
        messageSender.add(procedure);
        return this;
    }

    /**
     * Invokes all procedures
     *
     * @see Procedure#invoke()
     */
    public void execute() {
        messageSender.forEach(Procedure::invoke);
    }

}
