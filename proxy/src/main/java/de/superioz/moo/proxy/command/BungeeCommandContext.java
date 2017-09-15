package de.superioz.moo.proxy.command;

import de.superioz.moo.api.console.format.DisplayFormat;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.client.command.ClientCommandContext;
import de.superioz.moo.minecraft.chat.MessageComponent;
import de.superioz.moo.minecraft.util.ChatUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeeCommandContext extends ClientCommandContext<CommandSender> {

    public BungeeCommandContext(CommandSender commandSender) {
        super(commandSender);
    }

    @Override
    public void sendDisplayFormat(DisplayFormat format, CommandSender... receivers) {
        format.prepare();

        if(receivers == null || receivers.length == 0) receivers = new CommandSender[]{getCommandSender()};
        for(CommandSender receiver : receivers) {
            format.getComponents().forEach(stringBooleanPair
                    -> receiver.sendMessage(ChatUtil.getEventMessage(stringBooleanPair.getKey(), stringBooleanPair.getValue()).toTextComponent()));
        }
    }

    @Override
    public UUID getSendersUniqueId() {
        return getCommandSender() instanceof ProxiedPlayer
                ? ((ProxiedPlayer) getCommandSender()).getUniqueId() : CONSOLE_UUID;
    }

    @Override
    protected void message(String msg, CommandSender commandSender) {
        commandSender.sendMessage(new MessageComponent(msg).toTextComponent());
    }

    @Override
    public void sendCurrentUsage() {
        super.sendCurrentUsage(LanguageManager.get("usage-prefix"));
    }

}
