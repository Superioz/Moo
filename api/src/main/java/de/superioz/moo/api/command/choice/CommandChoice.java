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

    /**
     * Gets the option with given id
     *
     * @param id The id
     * @return The option
     */
    public CommandChoiceOption getOption(int id) {
        if(id >= options.size()) return CommandChoiceOption.EMPTY;
        return options.get(id);
    }

    /**
     * Adds multiple options to the command choice
     *
     * @param options The options to be added
     * @return This
     */
    public CommandChoice options(CommandChoiceOption... options) {
        this.options.addAll(Arrays.asList(options));
        return this;
    }

    /**
     * Adds one option to the command choice
     *
     * @param runnable The runnable (constructor of option)
     * @return This
     */
    public CommandChoice option(Runnable runnable) {
        return options(new CommandChoiceOption(runnable));
    }

    /**
     * Creates this command choice
     */
    public void create() {
        context.cycleChoice(this);
    }

}
