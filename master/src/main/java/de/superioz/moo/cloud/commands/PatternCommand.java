package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.database.objects.ServerPattern;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.api.utils.StringUtil;
import de.superioz.moo.cloud.common.PatternManager;
import de.superioz.moo.cloud.database.DatabaseCollections;

import java.util.Collections;
import java.util.List;

public class PatternCommand {

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.react(0, Collections.singletonList(
                StringUtil.format("&7Available subcommands: {{0}&7}",
                        StringUtil.getListToString(helper.getContext().getCommand().getChildrens(), ", ",
                                CommandInstance::getLabel))
                ), "pattern"
        );
    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.reactSubCommands("pattern");
    }

    @Command(label = "pattern", usage = "<subcommand>")
    public void onCommand(CommandContext context, ParamSet args) {
        // ..
    }

    @Command(label = "list", parent = "pattern")
    public void list(CommandContext context, ParamSet args) {
        List<ServerPattern> patterns = DatabaseCollections.PATTERN.list();
        context.invalidArgument(patterns.isEmpty(), "&cNo pattern found.");

        context.sendMessage("Patterns (" + patterns.size() + "):");
        patterns.forEach(pattern -> context.sendMessage("Pattern '" + pattern.getName() + "': {" + pattern + "}"));
    }

    @Command(label = "info", parent = "pattern", usage = "<name>")
    public void info(CommandContext context, ParamSet args) {
        String name = args.get(0);
        ServerPattern pattern = DatabaseCollections.PATTERN.get(name);
        context.invalidArgument(pattern == null, "&cThis pattern does not exist! (" + name + ")");

        context.sendMessage("Pattern '" + name + "': {" + pattern + "}");
    }

    @Command(label = "create", parent = "pattern", usage = "<name> [type] [priority] [min] [slots] [ram]")
    public void create(CommandContext context, ParamSet args) {
        String name = args.get(0);
        ServerPattern pattern = DatabaseCollections.PATTERN.get(name);
        context.invalidArgument(pattern != null, "&cThis pattern already exists! (" + name + ")");
        context.invalidArgument(!Validation.SIMPLE_NAME.matches(name), "&cWrong name format! (" + Validation.SIMPLE_NAME.getRawRegex() + ")");

        ServerPattern newPattern = new ServerPattern(name,
                args.getString(1, ServerPattern.DEFAULT_TYPE, Validation.SIMPLE_NAME::matches),
                args.getInt(2, ServerPattern.DEFAULT_PRIORITY),
                args.getInt(3, ServerPattern.DEFAULT_MIN),
                args.getInt(4, ServerPattern.DEFAULT_SLOTS),
                args.getString(5, ServerPattern.DEFAULT_RAM, Validation.RAM::matches)
        );
        context.invalidArgument(!PatternManager.getInstance().createPattern(newPattern), "&cCouldn't create pattern!");
        context.sendMessage("&aCreated pattern successfully.");
    }

    @Command(label = "delete", parent = "pattern", usage = "<name>")
    public void delete(CommandContext context, ParamSet args) {
        String name = args.get(0);
        ServerPattern pattern = DatabaseCollections.PATTERN.get(name);
        context.invalidArgument(pattern == null, "&cThis pattern does not exist! (" + name + ")");

        context.invalidArgument(!PatternManager.getInstance().deletePattern(pattern), "&cCouldn't delete pattern!");
        context.sendMessage("&aDeleted patter successfully.");
    }


}
