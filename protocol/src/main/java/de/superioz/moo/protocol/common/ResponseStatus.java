package de.superioz.moo.protocol.common;

import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.api.util.Validation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import de.superioz.moo.api.command.context.CommandContext;

/**
 * Status of a packet process which can be either positive or negative<br>
 * Similar to the HTTP responses
 */
@AllArgsConstructor
public enum ResponseStatus {

    /**
     * If the query was successful processed or if the query was allowed by the
     * target.
     * <br>
     * Summary: Just a sign that everything is fine
     */
    OK(0x00),

    /**
     * Opposite of {@link #OK}. Just like a parent response for every bad response.
     */
    NOK(0x40),

    /**
     * If the query was forbidden (not enough permissions, action not allowed, ..)
     */
    FORBIDDEN(0x42),

    /**
     * If the query wouldn't change anything (value was already set/value would not change the current state of the old value)
     */
    CONFLICT(0x43),

    /**
     * If the requested object or the target value field could not be found
     */
    NOT_FOUND(0x44),

    /**
     * If the request needs access to the database but the database is either
     * offline, the target is not connected to the database or if the target just
     * don't have access to the database currently
     */
    NO_DATABASE(0x80, true),

    /**
     * An exception prevents the request from being executed (for example NullPointer, ..)
     */
    INTERNAL_ERROR(0x81, true),

    /**
     * If the request's structure is invalid (invalid arguments [{@link Validation} for example],
     * invalid target, etc.)
     */
    BAD_REQUEST(0x82, true);

    @Getter
    private int id;
    @Getter
    private boolean critically;

    ResponseStatus(int id) {
        this(id, false);
    }

    /**
     * Simply turn the condition into a status
     *
     * @param condition The condition
     * @return The status (condition=true -> OK; otherwise: NOK)
     */
    public static ResponseStatus fromCondition(boolean condition) {
        return condition ? OK : NOK;
    }

    /**
     * Get the name of this status colored
     *
     * @return colored name
     */
    public String getColored() {
        return (this == OK ? ConsoleColor.GREEN : ConsoleColor.RED) + getName() + ConsoleColor.RESET;
    }

    /**
     * Gets the name (as lower case)
     *
     * @return The name as string
     */
    public String getName() {
        return name().toLowerCase();
    }

    /**
     * Checks if the status is ok
     *
     * @return The result
     */
    public boolean isOk() {
        return this == OK;
    }

    /**
     * Checks if the status is not ok
     *
     * @return The result
     */
    public boolean isNok() {
        return !isOk();
    }

    /**
     * Handles the state this response has
     *
     * @param context   The command context to send the message to the player
     * @param ok        The message if the status is OK
     * @param nok       status = NOK
     * @param forbidden status = FORBIDDEN
     * @param conflict  status = CONFLICT
     * @param notFound  status = NOT_FOUND
     */
    public void handleState(CommandContext context, String ok, String nok, String forbidden, String conflict, String notFound) {
        String message = "";

        // Could also use the ternary operator (:?) but switch should be faster with enums
        switch(this) {
            case OK:
                message = ok;
                break;
            case NOK:
                message = nok;
                break;
            case FORBIDDEN:
                message = forbidden;
                break;
            case CONFLICT:
                message = conflict;
                break;
            case NOT_FOUND:
                message = notFound;
                break;
        }

        if(!message.isEmpty()) {
            context.sendMessage(message);
        }
    }

    public void handleStateRoughly(CommandContext context, String ok, String nok) {
        handleState(context, ok, nok, nok, nok, nok);
    }

    /**
     * Similar to {@link #handleState(CommandContext, String, String, String, String, String)} but only handles the state
     * if the status is positive
     */
    public void handleStatePositively(CommandContext context, String ok) {
        handleStateRoughly(context, ok, "");
    }

    /**
     * Similar to {@link #handleState(CommandContext, String, String, String, String, String)} but only handles the state
     * if the status is negative
     */
    public void handleStateNegatively(CommandContext context, String nok, String forbidden, String conflict, String notFound) {
        handleState(context, "", nok, forbidden, conflict, notFound);
    }

    public void handleStateNegatively(CommandContext context, String nok) {
        handleStateRoughly(context, "", nok);
    }

}
