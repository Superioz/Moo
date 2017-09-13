package de.superioz.moo.client.command;

import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.context.MessageContextResult;
import de.superioz.moo.client.Moo;

public abstract class ClientCommandContext<T> extends CommandContext<T> {

    public ClientCommandContext(T commandSender) {
        super(commandSender);
    }

    /**
     * Sends a teamchat message ... simply
     *
     * @param msg          The message
     * @param replacements The replacements
     * @return The message context
     */
    public MessageContextResult<T> sendTeamChat(String msg, Object... replacements) {
        return super.sendMessage(Moo.getInstance().sendTeamChat(getSendersUniqueId(), msg, replacements));
    }
}
