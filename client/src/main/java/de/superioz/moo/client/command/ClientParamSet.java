package de.superioz.moo.client.command;

import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.network.common.MooPlayer;
import de.superioz.moo.network.server.MooProxy;

public abstract class ClientParamSet extends ParamSet {

    public ClientParamSet(CommandInstance commandInstance, String[] args) {
        super(commandInstance, args);
    }

    /**
     * Gets a MooPlayer with given name
     *
     * @param playerName The name
     * @return The player
     */
    public MooPlayer getMooPlayer(String playerName) {
        if(playerName == null || !Validation.PLAYERNAME.matches(playerName)) return null;
        return MooProxy.getInstance().getPlayer(playerName);
    }

    public MooPlayer getMooPlayer(int index) {
        return getMooPlayer(get(index));
    }

}
