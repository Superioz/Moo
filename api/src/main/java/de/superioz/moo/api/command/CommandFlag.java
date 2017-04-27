package de.superioz.moo.api.command;

import lombok.Getter;
import de.superioz.moo.api.command.param.GenericParameterSet;

import java.util.ArrayList;

@Getter
public class CommandFlag extends GenericParameterSet {

    /**
     * Specifies which argument from a command is a flag and which not
     */
    public static final String SPECIFIER = "-";

    /**
     * Label of the flag (full syntax would be: -label)
     *
     * @see #SPECIFIER
     */
    private String label;

    /**
     * Description of the flag (What does this flag do?)
     */
    private String description;

    public CommandFlag(String label, String description) {
        super(new ArrayList<>());
        this.label = label;
        this.description = description;
    }

}
