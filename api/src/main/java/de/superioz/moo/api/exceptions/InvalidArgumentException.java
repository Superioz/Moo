package de.superioz.moo.api.exceptions;

import lombok.Getter;
import de.superioz.moo.api.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * If an argument used for executing a command is invalid<br>
 * By calling this exception you can pass on objects to be inserted
 * into the error message string of the {@link Type}
 *
 * @see Type
 */
@Getter
public class InvalidArgumentException extends RuntimeException {

    private Type type;
    private boolean commandHelp;
    private List<Object> replacements;

    public InvalidArgumentException(Type type, Object... replacements) {
        this.type = type;
        this.replacements = Arrays.asList(replacements);
    }

    public InvalidArgumentException commandHelp(boolean commandHelp) {
        this.commandHelp = commandHelp;
        return this;
    }

    public enum Type {

        CUSTOM("{0}"),
        SIMPLE("Invalid argument ({0})!"),
        CONVERT("Invalid argument ({0})! Couldn't convert to {1}!"),
        VALIDATE("Invalid argument ({0})! It has to be {1}!");

        private String message;

        Type(String msg) {
            this.message = msg;
        }

        public String getMessage(List<Object> replacements) {
            if(replacements == null) replacements = new ArrayList<>();
            return StringUtil.format(message, replacements.toArray());
        }

    }

}
