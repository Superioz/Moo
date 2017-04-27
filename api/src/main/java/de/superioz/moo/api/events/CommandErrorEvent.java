package de.superioz.moo.api.events;

import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.event.Event;

/**
 * This events is fire when an error happens during executing a command
 *
 * @param <T>
 */
@Getter
public class CommandErrorEvent<T> implements Event {

    private CommandContext<T> context;
    private T commandSender;
    private CommandInstance instance;
    private Throwable exception;

    @Setter
    private String message;

    public CommandErrorEvent(CommandContext<T> context, CommandInstance instance, Throwable exception) {
        this.context = context;
        this.commandSender = context.getCommandSender();
        this.instance = instance;
        this.exception = exception.getCause() != null ? exception.getCause() : exception;
        this.message = this.exception.getMessage();
    }

}
