package de.superioz.moo.client.command;

import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.network.common.MooGroup;
import de.superioz.moo.network.common.MooPlayer;
import de.superioz.moo.network.common.MooProxy;

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
        if(playerName == null || !Validation.PLAYERNAME.matches(playerName)) {
            return new MooPlayer(null);
        }
        return MooProxy.getPlayer(playerName);
    }

    public MooPlayer getMooPlayer(int index) {
        return getMooPlayer(get(index));
    }

    /**
     * Gets a MooGroup with given name
     *
     * @param name The group's name
     * @return The group
     */
    public MooGroup getMooGroup(String name) {
        if(name == null || !Validation.SIMPLE_NAME.matches(name)) {
            return new MooGroup(null);
        }
        return MooProxy.getGroup(name);
    }

    public MooGroup getMooGroup(int index) {
        return getMooGroup(get(index));
    }

}
