package de.superioz.moo.proxy.command;

import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.minecraft.command.MinecraftCommandContext;
import de.superioz.moo.proxy.util.BungeeChat;
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
