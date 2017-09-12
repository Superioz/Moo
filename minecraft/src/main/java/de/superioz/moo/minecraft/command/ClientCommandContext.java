package de.superioz.moo.minecraft.command;

import de.superioz.moo.api.command.context.CommandContext;

public abstract class ClientCommandContext<T> extends CommandContext<T> {

    public ClientCommandContext(T commandSender) {
        super(commandSender);
    }

}
