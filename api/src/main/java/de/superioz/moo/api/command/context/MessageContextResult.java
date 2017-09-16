package de.superioz.moo.api.command.context;

import lombok.Getter;

@Getter
public class MessageContextResult<T> {

    /**
     * The context from where the message has been sent (or not)
     */
    private CommandContext<T> context;

    /**
     * The message that has been sent (or not)
     */
    private String message;

    /**
     * The status if the message has been sent. If not that could mean that the console cannot do! (maybe because
     * click/hover event)
     */
    private boolean sent;

    /**
     * If the message chain got to this point
     */
    private boolean reached;

    public MessageContextResult(CommandContext<T> context, String message, boolean sent, boolean reached) {
        this.context = context;
        this.message = message;
        this.sent = sent;
        this.reached = reached;
    }

    /**
     * If the message couldn't be sent properly, let's try it with another message!
     *
     * @param msg          The message
     * @param replacements The replacements
     * @return The result
     */
    public MessageContextResult<T> or(String msg, Object... replacements) {
        if(sent) return new MessageContextResult<>(context, msg, false, false);
        return context.sendMessage(msg, replacements);
    }

    public MessageContextResult<T> or(boolean condition, String msg, Object... replacements) {
        if(!condition) return new MessageContextResult<>(context, msg, false, true);
        return or(msg, replacements);
    }

    /**
     * If the message couldn't be sent properly, let's try a complete different way!
     *
     * @param runnable The runnable, where you choose what we're gonna do
     */
    public void or(Runnable runnable) {
        if(!sent) runnable.run();
    }

}
