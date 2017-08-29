package de.superioz.moo.spigot.listeners;

import de.superioz.moo.api.database.objects.Group;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.netty.common.MooQueries;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Group group = MooQueries.getInstance().getGroup(player.getUniqueId());

        String format = LanguageManager.get("chat-message-pattern");
        format = LanguageManager.format(format, group.getPrefix(), group.getColor(), "%1$s", group.getSuffix(), "%2$s");

        event.setFormat(format);
    }

}
