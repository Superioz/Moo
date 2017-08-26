package de.superioz.moo.cloud.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.utils.StringUtil;

import java.util.Collections;

public class PatternCommand {

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.react(0, Collections.singletonList(
                StringUtil.format("&7Available subcommands: {{0}&7}",
                        StringUtil.getListToString(helper.getContext().getCommand().getChildrens(), ", ",
                                CommandInstance::getLabel))
                )
        );
    }

    @TabCompletion
    public void onTabComplete(TabCompletor completor) {
        completor.reactSubCommands("pattern");
    }

    @Command(label = "pattern")
    public void onCommand(CommandContext context, ParamSet args) {

    }

    @Command(label = "info", parent = "pattern", usage = "<name>")
    public void info(CommandContext context, ParamSet args) {

    }

    @Command(label = "create", parent = "pattern", usage = "<name> <type> <priority> <min> <max> <ram>")
    public void create(CommandContext context, ParamSet args) {

    }

    @Command(label = "delete", parent = "pattern", usage = "<name>")
    public void delete(CommandContext context, ParamSet args) {

    }

    @Command(label = "check", parent = "pattern", usage = "<name>")
    public void check(CommandContext context, ParamSet args) {

    }

}
