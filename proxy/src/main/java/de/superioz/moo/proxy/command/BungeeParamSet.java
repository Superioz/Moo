package de.superioz.moo.proxy.command;

import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.client.command.ClientParamSet;

public class BungeeParamSet extends ClientParamSet {

    public BungeeParamSet(CommandInstance commandInstance, String[] args) {
        super(commandInstance, args);
    }

}
