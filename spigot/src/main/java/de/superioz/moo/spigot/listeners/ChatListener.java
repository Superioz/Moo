package de.superioz.moo.spigot.listeners;

import de.superioz.moo.api.database.object.Group;
import de.superioz.moo.client.common.MooQueries;
import de.superioz.moo.spigot.util.LanguageManager;
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
        format = LanguageManager.format(format, group.prefix, group.color, "%1$s", group.suffix, "%2$s");

        event.setFormat(format);
    }

}
