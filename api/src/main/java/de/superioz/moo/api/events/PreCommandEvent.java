package de.superioz.moo.api.events;

import de.superioz.moo.api.event.Cancellable;
import lombok.Getter;
import lombok.Setter;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.event.Event;

import java.util.concurrent.ExecutorService;

/**
 * This events is called when during executing a command the system is walking down a level.
 * This happens even before the first level!
 * In this events you can check the permission, etc.
 *
 * @param <T>
 */
@Getter
public class PreCommandEvent<T> implements Event, Cancellable {

    private CommandContext<T> context;
    private T commandSender;
    private CommandInstance newInstance;
    @Setter
    private ExecutorService service;

    private boolean cancelled;

    public PreCommandEvent(CommandContext<T> context, CommandInstance newInstance) {
        this.context = context;
        this.commandSender = context.getCommandSender();
        this.newInstance = newInstance;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

}
