package de.superioz.moo.api.command.choice;

import lombok.Getter;

@Getter
public class CommandChoiceOption {

    /**
     * When the option got chosen
     */
    private Runnable runnable;

    public CommandChoiceOption(Runnable runnable) {
        this.runnable = runnable;
    }

}
