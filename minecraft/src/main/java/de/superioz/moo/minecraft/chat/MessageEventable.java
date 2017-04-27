package de.superioz.moo.minecraft.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

@AllArgsConstructor
@Getter
public class MessageEventable {

    private HoverEvent.Action hoverAction;
    private ClickEvent.Action clickAction;
    private boolean condition;

    public MessageEventable(HoverEvent.Action hoverAction, ClickEvent.Action clickAction) {
        this(hoverAction, clickAction, true);
    }

    public MessageEventable(ClickEvent.Action clickAction) {
        this(null, clickAction);
    }

}
