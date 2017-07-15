package de.superioz.moo.proxy.commands;

import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.proxy.command.BungeeCommandContext;

@RunAsynchronous
public class TeamCommand {

    private static final String LABEL = "team";

    @Command(label = LABEL)
    public void onCommand(BungeeCommandContext context, ParamSet args) {

    }

}
