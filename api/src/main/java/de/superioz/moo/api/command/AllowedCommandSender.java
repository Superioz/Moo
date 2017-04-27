package de.superioz.moo.api.command;

import de.superioz.moo.api.console.CommandTerminal;

public enum AllowedCommandSender {

    /**
     * Not needed with {@link CommandTerminal} but with minecraft
     * the user is the player who executes a command
     */
    USER,

    /**
     * The console who executes the command.
     */
    CONSOLE,

    /**
     * Either {@link #USER} or {@link #CONSOLE} is allowed
     */
    BOTH

}
