package de.superioz.moo.proxy.listeners;

import de.superioz.moo.api.command.*;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventHandler;
import de.superioz.moo.api.events.CommandErrorEvent;
import de.superioz.moo.api.events.CommandHelpEvent;
import de.superioz.moo.api.events.PreCommandEvent;
import de.superioz.moo.api.events.TabCompleteEvent;
import de.superioz.moo.api.exceptions.InvalidArgumentException;
import de.superioz.moo.api.exceptions.InvalidCommandUsageException;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.MessageFormatSender;
import de.superioz.moo.api.utils.DisplayFormats;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.protocol.exception.MooOutputException;
import de.superioz.moo.proxy.Thunder;
import de.superioz.moo.proxy.command.BungeeCommandContext;
import javafx.util.Pair;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.List;

public class CommandListener extends CommandEventAdapter<CommandSender> implements Listener {

    @net.md_5.bungee.event.EventHandler
    public void onBungeeTabComplete(net.md_5.bungee.api.event.TabCompleteEvent event) {
        if(event.isCancelled()) return;
        String cursor = event.getCursor();
        if(cursor.startsWith("/")) cursor = cursor.substring(1, cursor.length());

        TabCompleteEvent e = new TabCompleteEvent(cursor);
        EventExecutor.getInstance().execute(e);
        if(!e.isCancelled()) {
            List<String> l = e.getSuggestions();
            if(l != null) event.getSuggestions().addAll(l);
        }
    }

    @Override
    public void onTabComplete(TabCompleteEvent event) {
        if(event.getArgumentsSize() == 1) {
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

        // add all suggestions to the list which starts with the cursor
        List<String> suggestions = new ArrayList<>();
        completor.getElement().forEach(s -> {
            if(s.startsWith(event.getCurrentBuffer())) {
                suggestions.add(s);
            }
        });
        event.setSuggestions(suggestions);
    }

    @Override
    public void onCommandError(CommandErrorEvent<CommandSender> event) {
        Throwable exception = event.getException();
        CommandContext context = event.getContext();

        // user used an invalid argument
        if(exception instanceof InvalidArgumentException) {
            InvalidArgumentException e = (InvalidArgumentException) exception;
            context.sendMessage("&c" + e.getType().getMessage(e.getReplacements()));

            if(e.isCommandHelp()) {
                context.sendHelpCurrent(true);
            }
        }
        // wrong command usage (not enough arguments, ..)
        else if(exception instanceof InvalidCommandUsageException) {
            InvalidCommandUsageException e = (InvalidCommandUsageException) exception;

            if(e.getType() == InvalidCommandUsageException.Type.NOT_ALLOWED) {
                context.sendMessage(LanguageManager.get("error-not-allowed-execute-command"));
                return;
            }

            context.sendUsage(LanguageManager.get("usage-prefix"), true);
            context.sendHelp(e.getHelpParams());
        }
        // moo exception
        else if(exception instanceof MooOutputException) {
            MooOutputException e = (MooOutputException) exception;
            if(e.getType() == MooOutputException.Type.CONNECTION_FAILED) {
                context.sendMessage(LanguageManager.get("error-no-moo-connection"));
            }
            else {
                context.sendMessage(e.getMessage());
            }
        }
        // cannot handle exception
        else {
            context.sendMessage(LanguageManager.get("error-while-command-execution", exception.getClass().getSimpleName()));
            System.err.println("Error while executing command: ");
            exception.printStackTrace();
        }
    }

    @Override
    public void onCommandHelp(CommandHelpEvent<CommandSender> event) {
        BungeeCommandContext context = (BungeeCommandContext) event.getContext();

        // checks the permission
        if(!context.getCommandSender().hasPermission(Thunder.getInstance().getPluginModule().getConfig().get("command-help-permission"))) {
            return;
        }
        CommandInstance instance = context.getCommand();

        // send the command help
        // if the instance has children show a list of subcommands
        if(instance.hasChildren()) {
            MessageFormatSender formatSender = context.getFormatSender(LanguageManager.get("help-command-leaf-entry"));
            ChatColor[] gradient = ChatUtil.GRADIENT_BLUE;

            for(CommandInstance children : instance.getChildrens()) {
                List<String> wholePath = children.getWholePath();

                // gets the usages for the format
                String suggestUsage = String.join(" ", wholePath);
                List<String> coloredPath = new ArrayList<>();
                for(int i = 0; i < wholePath.size(); i++) {
                    String color = "&f";
                    if(i < gradient.length) {
                        color = gradient[i] + "";
                    }
                    coloredPath.add(color + wholePath.get(i));
                }

                formatSender.add(() -> {
                    context.sendEventMessage(StringUtil.format(formatSender.getFormat(), s -> LanguageManager.get(s),
                            String.join(" ", coloredPath) + " &7" + children.getUsage().getBase(),
                            suggestUsage, suggestUsage));
                });
            }

            DisplayFormats.sendList(context,
                    LanguageManager.get("help-command-leaf-header", instance.getLabel()),
                    formatSender);
        }
        // if the instance has NO children show a detailed command info
        else {
            List<String> flags = StringUtil.getStringList(instance.getFlags(), s -> {
                CommandFlag flag = instance.getFlagBase(s);
                return "&f-" + flag.getLabel() + " &7" + flag.getDescription();
            });

            // send command help as detailed list
            context.sendMessage(LanguageManager.get("help-command-leaf-header", instance.getLabel()));
            context.sendMessage(instance.getDescription().isEmpty()
                    ? LanguageManager.get("help-command-leaf-no-desc")
                    : LanguageManager.get("help-command-leaf-desc", instance.getDescription()));

            // send whole usage
            boolean hasParent = instance.hasParent();
            context.sendEventMessage(
                    LanguageManager.get("help-command-leaf-usage",
                            hasParent ? ".." : "&f" + instance.getLabel(),
                            hasParent ? String.join(" ", instance.getBeforePath()) : "",
                            (hasParent ? "&f" + instance.getLabel() + " " : "") + "&7" + instance.getUsage().getBase()),
                    hasParent
            );

            // parents and flags
            if(hasParent) {
                context.sendMessage(LanguageManager.get("help-command-leaf-parent", instance.getParentName()));
            }

            context.sendEventMessage(
                    LanguageManager.get("help-command-leaf-flags", flags.size(), String.join("\n", flags)),
                    flags.size() > 0
            );
        }
        event.setCancelled(true);
    }

    @EventHandler
    public void onPreCommand(PreCommandEvent<CommandSender> event) {
        CommandSender sender = event.getCommandSender();
        CommandInstance command = event.getNewInstance();

        if(command.getMethod().isAnnotationPresent(RunAsynchronous.class)
                || command.getMethodClassObject().getClass().isAnnotationPresent(RunAsynchronous.class)) {
            event.setService(Thunder.getInstance().getExecutorService());
        }

        // CHECK SENDER
        boolean isPlayer = sender instanceof ProxiedPlayer;
        if(isPlayer && command.getCommandTarget() == AllowedCommandSender.CONSOLE) {
            EventExecutor.getInstance().execute(new CommandErrorEvent<>(event.getContext(), command,
                    new InvalidCommandUsageException(InvalidCommandUsageException.Type.NOT_ALLOWED, command)));
            event.setCancelled(true);
        }
        else if(!isPlayer && command.getCommandTarget() == AllowedCommandSender.USER) {
            EventExecutor.getInstance().execute(new CommandErrorEvent<>(event.getContext(), command,
                    new InvalidCommandUsageException(InvalidCommandUsageException.Type.NOT_ALLOWED, command)));
            event.setCancelled(true);
        }
    }

}
