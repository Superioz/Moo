package de.superioz.moo.proxy.command;

import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.minecraft.chat.formats.DisplayFormat;
import de.superioz.moo.minecraft.command.MinecraftCommandContext;
import de.superioz.moo.proxy.util.BungeeChat;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.UUID;

public class BungeeCommandContext extends MinecraftCommandContext<CommandSender> {

    public BungeeCommandContext(CommandSender commandSender) {
        super(commandSender);
    }

    @Override
    public void sendDisplayFormat(DisplayFormat format, CommandSender... receivers) {
        format.prepare();

        if(receivers == null || receivers.length == 0) receivers = new CommandSender[]{getCommandSender()};
        for(CommandSender receiver : receivers) {
            format.getComponents().forEach(receiver::sendMessage);
        }
    }

    @Override
    public BungeeTeamChat getTeamChat() {
        if(teamChat != null) return (BungeeTeamChat) teamChat;
        teamChat = new BungeeTeamChat();
        return getTeamChat();
    }

    @Override
    protected UUID getSendersUniqueId() {
        return getCommandSender() instanceof ProxiedPlayer
                ? ((ProxiedPlayer) getCommandSender()).getUniqueId() : CONSOLE_UUID;
    }

    @Override
    protected void message(String msg, CommandSender commandSender) {
        BungeeChat.send(msg, commandSender);
    }

    @Override
    public void sendCurrentUsage() {
        super.sendCurrentUsage(LanguageManager.get("usage-prefix"));
    }

    @Override
    public void sendComponent(TextComponent component, List<CommandSender> receiver) {
        BungeeChat.send(component, receiver.toArray(new CommandSender[]{}));
    }

}
