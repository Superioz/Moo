package de.superioz.moo.api.command.tabcomplete;

import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.events.TabCompleteEvent;
import de.superioz.moo.api.reaction.Reactable;
import de.superioz.moo.api.util.Procedure;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;
import de.superioz.moo.api.command.param.GenericParameterSet;

import java.util.ArrayList;
import java.util.List;

/**
 * A reactor for the {@link TabCompletion} of a command execution
 */
@Getter
public class TabCompletor extends Reactable<List<String>> {

    /**
     * The parameter of the command execution
     */
    private GenericParameterSet parameterSet;

    /**
     * The command itself
     */
    private CommandInstance command;

    // sets the element
    {
        super.element = new ArrayList<>();
    }

    public TabCompletor(CommandInstance command, TabCompleteEvent event) {
        this.parameterSet = GenericParameterSet.newInstance(event.getParameter().subList(1, event.getParameter().size()));
        this.command = command;

        this.key = command.getPath();
        this.id = parameterSet.size();
    }

    /**
     * React with the subcommands of the tabCompletor's command
     *
     * @param keys The keys
     */
    public void reactSubCommands(String... keys) {
        react(1, StringUtil.getStringList(getCommand().getChildrens(),
                CommandInstance::getLabel
        ), keys);
    }

    /**
     * React to the before argument (shifted 'index' to the left) if it is a flag
     *
     * @param flag      The flag without '-'
     * @param index     The index to be shifted
     * @param procedure The procedure
     * @param keys      The keys
     */
    public void react(String flag, int index, Procedure procedure, String... keys) {
        String argumentToCheck = parameterSet.getBefore(index);
        if(argumentToCheck == null) return;

        // if the key is correct AND the argument is correct like '-flag'
        if((keys.length == 0 || checkKey(keys))
                && argumentToCheck.equalsIgnoreCase("-" + flag)) {
            procedure.invoke();
        }
    }

    public void react(String flag, int index, List<String> elements, String... keys) {
        react(flag, index, () -> setSuggestions(elements), keys);
    }

    public void react(String flag, Procedure procedure, String... keys) {
        react(flag, 1, procedure, keys);
    }

    public void react(String flag, List<String> elements, String... keys) {
        react(flag, 1, elements, keys);
    }

    public void setSuggestions(List<String> l) {
        this.element = l;
    }

}
