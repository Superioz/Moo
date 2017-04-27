package de.superioz.moo.proxy.util;

import de.superioz.moo.minecraft.util.ChatUtil;
import de.superioz.moo.proxy.Thunder;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;

public class BungeeChat {

    /**
     * Broadcasts a message
     *
     * @param message The message
     */
    public static void broadcast(BaseComponent[] message) {
        Collection<ProxiedPlayer> players = Thunder.getInstance().getProxy().getPlayers();
        send(message, players.toArray(new ProxiedPlayer[players.size()]));
    }

    public static void broadcast(BaseComponent message) {
        broadcast(new BaseComponent[]{message});
    }

    public static void broadcast(String message) {
        Collection<ProxiedPlayer> players = Thunder.getInstance().getProxy().getPlayers();
        send(message, players.toArray(new ProxiedPlayer[players.size()]));
    }

    public static void broadcast(String message, String prefix) {
        Collection<ProxiedPlayer> players = Thunder.getInstance().getProxy().getPlayers();
        send(message, prefix, players.toArray(new ProxiedPlayer[players.size()]));
    }

    /**
     * Sends a message to given players with prefix (message)
     *
     * @param message The message
     * @param prefix  The prefix
     * @param players The players
     */
    public static void send(String message, String prefix, ProxiedPlayer... players) {
        send(prefix + " " + message, players);
    }

    public static void send(String message, String prefix, CommandSender... senders) {
        send(prefix + " " + message, senders);
    }

    /**
     * Sends a message to given players (baseComponent)
     *
     * @param message The message
     * @param players The players
     */
    public static void send(BaseComponent[] message, ChatMessageType type, ProxiedPlayer... players) {
        for(ProxiedPlayer p : players) {
            if(message.length == 1){
                p.sendMessage(type, message[0]);
                continue;
            }
            p.sendMessage(type, message);
        }
    }

    public static void send(BaseComponent message, ChatMessageType type, ProxiedPlayer... players){
        send(new BaseComponent[]{message}, type, players);
    }

    public static void send(BaseComponent[] message, CommandSender... senders) {
        for(CommandSender s : senders) {
            if(message.length == 1){
                s.sendMessage(message[0]);
                continue;
            }
            s.sendMessage(message);
        }
    }

    public static void send(BaseComponent message, CommandSender... senders){
        send(new BaseComponent[]{message}, senders);
    }

    /**
     * Sends a message to given players (string)
     *
     * @param message The message
     * @param players The players
     */
    public static void send(String message, ChatMessageType type, ProxiedPlayer... players) {
        send(TextComponent.fromLegacyText(ChatUtil.fabulize(message)), type, players);
    }

    public static void send(BaseComponent[] message, ProxiedPlayer... players) {
        send(message, ChatMessageType.CHAT, players);
    }

    public static void send(String message, CommandSender... senders) {
        send(TextComponent.fromLegacyText(ChatUtil.fabulize(message)), senders);
    }

    public static void send(String message, ProxiedPlayer... players) {
        send(message, ChatMessageType.CHAT, players);
    }

}
