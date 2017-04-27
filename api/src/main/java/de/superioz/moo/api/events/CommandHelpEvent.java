package de.superioz.moo.api.events;

import de.superioz.moo.api.event.Cancellable;
import lombok.Getter;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.event.Event;

/**
 * This event is for sending help about one specific command, that means the
 * user used the '-?' flag<br>
 * Cancelling this event means you've handled the event successfully
 */
@Getter
public class CommandHelpEvent<T> implements Event, Cancellable {

    private CommandContext<T> context;
    private boolean cancelled = false;

    public CommandHelpEvent(CommandContext context) {
        this.context = context;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
