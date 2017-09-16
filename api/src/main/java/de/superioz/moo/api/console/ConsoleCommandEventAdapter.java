package de.superioz.moo.api.console;

import de.superioz.moo.api.events.CommandErrorEvent;
import de.superioz.moo.api.events.CommandHelpEvent;
import de.superioz.moo.api.events.TabCompleteEvent;
import de.superioz.moo.api.exceptions.CommandException;
import javafx.util.Pair;
import de.superioz.moo.api.command.CommandEventAdapter;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.exceptions.InvalidCommandUsageException;

public class ConsoleCommandEventAdapter extends CommandEventAdapter {

    @Override
    public void onTabComplete(TabCompleteEvent event) {
        if(event.getArgumentsSize() == 1) {
            event.setSuggestions(CommandRegistry.getInstance().getCommands());
            return;
        }

        String firstArg = event.getParameter().get(0);
        CommandInstance instance = CommandRegistry.getInstance().getCommand(firstArg);
        Pair<CommandInstance, String[]> context
                = instance.getInstance(event.getParameter().subList(1, event.getParameter().size())
                .toArray(new String[]{}), null);
        if(instance == null || context == null) {
            return;
        }
        instance = context.getKey();

        TabCompletor completor = new TabCompletor(instance, event);
        instance.executeTabCompletion(completor);
        event.setSuggestions(completor.getElement());
    }

    @Override
    public void onCommandError(CommandErrorEvent event) {
        Throwable exception = event.getException();
        CommandContext context = event.getContext();

        if(exception instanceof CommandException) {
            CommandException e = (CommandException) exception;
            context.sendMessage("§c" + e.getType().getMessage(e.getReplacements()));

            if(e.isCommandHelp()) {
                context.sendHelpCurrent(true);
            }
        }
        else if(exception instanceof InvalidCommandUsageException) {
            InvalidCommandUsageException e = (InvalidCommandUsageException) exception;

            if(e.getType() == InvalidCommandUsageException.Type.NOT_ALLOWED) {
                context.sendMessage("§4You are not allowed to execute this command!");
                return;
            }

            context.sendUsage("§cWrong usage! ", true);
            context.sendHelp(e.getHelpParams());
        }
        else {
            context.sendMessage("§4Error while executing the command (" + exception.getClass().getSimpleName() + ")!");
            System.err.println("Error while executing command: ");
            exception.printStackTrace();
        }
    }

    @Override
    public void onCommandHelp(CommandHelpEvent event) {
        CommandContext context = event.getContext();
        CommandInstance instance = context.getCommand();

        context.sendMessage("Commandhelp for '" + instance.getLabel() + "':");
        context.sendMessage("- Syntax: " + instance.getLabel() + " " + instance.getUsage().getBase());
        context.sendMessage("- Aliases: " + instance.getAliases());
        context.sendMessage("- Description: " + instance.getDescription());
        context.sendMessage("- Source: " + instance.getMethod().getDeclaringClass().getSimpleName() + "#" + instance.getMethod().getName() + "()");

        event.setCancelled(true);
    }
}
