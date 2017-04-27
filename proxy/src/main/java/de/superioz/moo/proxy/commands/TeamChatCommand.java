package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.proxy.util.BungeeTeamChat;
import de.superioz.moo.proxy.command.BungeeCommandContext;

@RunAsynchronous
public class TeamChatCommand {

    private static final String LABEL = "teamchat";

    @Command(label = LABEL, usage = "<message>", flags = {"r"},
            aliases = "tc")
    public void onCommand(BungeeCommandContext context, ParamSet args) {
        String message = String.join(" ", args.getRange(0));
        boolean colored = true;
        boolean formatted = true;

        // get sender's name
        // and the permission of color/format
        String sender = BungeeTeamChat.getInstance().getColoredName(context.getCommandSender());

        // get team chat format (either the message or blank format)
        // if flag r (=raw) exists ^
        String formattedMessage = args.hasFlag("r")
                ? LanguageManager.get("teamchat-format", "/tc ", message)
                : LanguageManager.get("teamchat-format-message", "/tc ", sender, message);

        // dispatch message
        BungeeTeamChat.getInstance().send(formattedMessage, colored, formatted);
    }

}
