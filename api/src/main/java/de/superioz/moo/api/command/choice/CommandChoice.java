package de.superioz.moo.api.command.choice;

import de.superioz.moo.api.command.context.CommandContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class CommandChoice {

    private CommandContext context;
    private String message;
    private List<CommandChoiceOption> options = new ArrayList<>();

    public CommandChoice(CommandContext context, String message) {
        this.context = context;
        this.message = message;
    }

    public CommandChoice options(CommandChoiceOption... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    public CommandChoice option(Runnable runnable) {
        return options(new CommandChoiceOption(runnable));
    }

    public void create() {
        context.createChoice(this);
    }

}
