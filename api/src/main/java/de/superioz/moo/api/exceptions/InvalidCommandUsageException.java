package de.superioz.moo.api.exceptions;

import de.superioz.moo.api.command.help.ArgumentHelper;
import lombok.Getter;
import de.superioz.moo.api.command.CommandInstance;

/**
 * Error if the command usage is invalid, e.g. too few arguments
 *
 * @see Type
 */
@Getter
public class InvalidCommandUsageException extends RuntimeException {

    private Type type;
    private CommandInstance command;
    private Object[] helpParams;

    public InvalidCommandUsageException(Type type, CommandInstance command, Object... helpParams) {
        super("Invalid usage: " + type);
        this.type = type;
        this.command = command;
        this.helpParams = helpParams;
    }

    public enum Type {

        /**
         * The player used too few arguments
         */
        TOO_FEW_ARGUMENTS,

        /**
         * The player is not allowed to use this command
         */
        NOT_ALLOWED,

        /**
         * Custom wrong usage message with triggering the {@link ArgumentHelper}
         */
        CUSTOM_EVENTABLE

    }


}
