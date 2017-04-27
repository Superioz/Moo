package de.superioz.moo.api.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    /**
     * The label of the command the user has to use
     * Example: /fly ('fly' is here the label)
     * If you use the command annotation on a subCommand then the label
     * is the label of the subcommand.
     * Example: /fly change .. ('change' is the label of the subcommand)
     *
     * @return the label
     */
    String label();

    /**
     * Aliases for the label {@link #label()}
     * That means the user can either use the label or one of the aliases
     * to execute the command
     *
     * @return The aliases
     */
    String[] aliases() default {""};

    /**
     * Parent of the command/subcommand
     *
     * @return The parents
     */
    String parent() default "";

    /**
     * The description of the command to tell the user what the command does.
     * Example: Changes your gamemode (for the command /gamemode)
     *
     * @return The description
     */
    String desc() default "";

    /**
     * The permission the user needs to execute this command.
     * This also works with subCommands.
     * Example: plugin.fly (for the command fly)
     * Or if you want to set the permission for a subCommand: plugin.fly.other (Influencing another player)
     *
     * @return The permission
     */
    String permission() default "";

    /**
     * Flags for the command which are supported. That means if you want to use flags in combination
     * with the other parts of the command system list them here.<br>
     * Example: flags = {"?[shows the help menu]"}, where the string inside the [] brackets is the description<br>
     * A flag has to be at the end of the command usage
     *
     * @return The flags
     */
    String[] flags() default {""};

    /**
     * The usage of this command to show how the user has to use the command
     * Example: "change {@literal <}mode{@literal >} {@literal <}player{@literal >}" for the fly command.
     * This would mean: "/fly change {@literal <}mode{@literal >} {@literal <}player{@literal >}" for the usage
     *
     * @return The usage
     */
    String usage() default "";

    /**
     * The command sender that is allowed to use the command<br>
     * For example: If you want to let only the console use this command use {@link AllowedCommandSender#CONSOLE}
     *
     * @return The command target
     */
    AllowedCommandSender commandTarget() default AllowedCommandSender.BOTH;

}
