package de.superioz.moo.minecraft.chat;

import java.util.UUID;

public abstract class TeamChat<T, R> {

    public static final String RANK_KEY = "team-rank";

    /**
     * Sends a message into the team chat
     *
     * @param formattedMessage The formatted message (sender + message inside)
     * @param colored          Can the user use colored text
     * @param formatted        Can the user use formatted text
     */
    public abstract R send(String formattedMessage, boolean colored, boolean formatted);

    public R send(String formattedMessage) {
        return send(formattedMessage, true, true);
    }

    /**
     * Checks if the user can write into the teamchat
     *
     * @param t The user
     * @return The result
     */
    public abstract boolean canTeamchat(T t);

    /**
     * Geets the colored name of given t (= CommandSender for example)
     *
     * @param t The object (player/console/..)
     * @return The color + name
     */
    public abstract String getColoredName(T t);

    public abstract String getColor(UUID uuid);

}
