package de.superioz.moo.api.command.help;

import de.superioz.moo.api.reaction.Reactable;
import lombok.Getter;
import de.superioz.moo.api.command.context.CommandContext;

import java.util.*;

/**
 * A reactor for the {@link ArgumentHelp} during a command execution
 *
 * @param <T> The type of commandSender
 */
@Getter
public class ArgumentHelper<T> extends Reactable<List<String>> {

    /**
     * Parameter which can be used to specify the help more
     */
    private Set<Object> parameter;

    /**
     * The context of the command execution
     */
    private CommandContext<T> context;

    // sets the element of the parent
    {
        super.element = new ArrayList<>();
    }

    public ArgumentHelper(CommandContext<T> context, int id, Object... parameter) {
        this.key = context.getCommand().getPath();
        this.id = id;

        this.context = context;
        this.parameter = new HashSet<>(Arrays.asList(parameter));
    }

    /**
     * Gets a parameter from {@link #parameter} only by using the parameter's class
     *
     * @param eClass The parameter class
     * @param <E>    The generic type of the class
     * @return The parameter object
     */
    public <E> E getParam(Class<E> eClass) {
        for(Object param : parameter) {
            if(param != null && eClass.isAssignableFrom(param.getClass())) {
                return (E) param;
            }
        }
        return null;
    }

    /**
     * Sets the element of the reactable (another method name)
     *
     * @param messages The messages to be sent to the sender as help
     */
    public void setHelpMessages(String... messages) {
        this.element = Arrays.asList(messages);
    }

}
