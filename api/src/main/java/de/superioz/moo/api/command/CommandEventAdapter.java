package de.superioz.moo.api.command;

import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.events.CommandErrorEvent;
import de.superioz.moo.api.events.CommandHelpEvent;
import de.superioz.moo.api.events.TabCompleteEvent;
import de.superioz.moo.api.event.EventListener;

/**
 * This class is for implementing all important command listener methods<br>
 * Includes: {@link TabCompleteEvent}, {@link CommandErrorEvent}, {@link CommandHelpEvent}
 *
 * @param <T> The type of the command context
 */
public abstract class CommandEventAdapter<T> implements EventListener {

    @EventHandler
    private void tabComplete(TabCompleteEvent event) {
        onTabComplete(event);
    }

    /**
     * This event is for getting possibilities for completing an argument
     *
     * @param event The event
     */
    public abstract void onTabComplete(TabCompleteEvent event);

    @EventHandler
    private void commandError(CommandErrorEvent<T> event) {
        onCommandError(event);
    }

    /**
     * This event is for handling an error during the command process
     *
     * @param event The event
     */
    public abstract void onCommandError(CommandErrorEvent<T> event);

    @EventHandler
    private void commandHelp(CommandHelpEvent<T> event) {
        onCommandHelp(event);
    }

    /**
     * This event is for handling the help flag '-?' during the command process
     *
     * @param event The event
     */
    public abstract void onCommandHelp(CommandHelpEvent<T> event);


}
