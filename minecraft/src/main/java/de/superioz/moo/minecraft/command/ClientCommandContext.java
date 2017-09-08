package de.superioz.moo.minecraft.command;

import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.minecraft.chat.MessageComponent;
import de.superioz.moo.minecraft.chat.MessageEventable;
import de.superioz.moo.minecraft.util.ChatUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class ClientCommandContext<T> extends CommandContext<T> {

    public ClientCommandContext(T commandSender) {
        super(commandSender);
    }

    public abstract void sendComponent(TextComponent component, List<T> receivers);

    /**
     * Sends a message component to given command sender (with hoverevent and this stuff)
     *
     * @param component  The message component
     * @param receiver   The receiver of this message
     * @param eventables The eventables to format the component
     */
    public void sendEventMessage(MessageComponent component, List<T> receiver, MessageEventable... eventables) {
        if(receiver == null || receiver.isEmpty()) {
            receiver = Collections.singletonList(getCommandSender());
        }
        if(eventables.length != 0) {
            component = ChatUtil.getEventMessage(component, eventables);
        }
        sendComponent(component.toTextComponent(), receiver);
    }

    /**
     * Similar to {@link #sendEventMessage(MessageComponent, List, MessageEventable...)}
     * but with a string message instead of a message component
     *
     * @param message    The message
     * @param receiver   The receiver of this message
     * @param eventables The eventables to format the component
     */
    public void sendEventMessage(String message, List<T> receiver, MessageEventable... eventables) {
        sendEventMessage(ChatUtil.getEventMessage(message, eventables), receiver);
    }

    public void sendEventMessage(String message, ClickEvent.Action clickAction, boolean condition, T... receiver) {
        sendEventMessage(ChatUtil.getEventMessage(message, clickAction, condition), Arrays.asList(receiver));
    }

    public void sendEventMessage(String message, ClickEvent.Action clickAction, T... receiver) {
        sendEventMessage(ChatUtil.getEventMessage(message, clickAction), Arrays.asList(receiver));
    }

    public void sendEventMessage(String message, boolean condition, T... receiver) {
        sendEventMessage(ChatUtil.getEventMessage(message, condition), Arrays.asList(receiver));
    }

    public void sendEventMessage(String message, T... receiver) {
        sendEventMessage(message, Arrays.asList(receiver));
    }

}
