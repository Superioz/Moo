package de.superioz.moo.client.command.params;

import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.common.PlayerProfile;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.network.common.MooQueries;

import java.util.UUID;

public class PlayerInfoParamType extends ParamType<PlayerProfile> {

    @Override
    public String label() {
        return "playerinfo";
    }

    @Override
    public PlayerProfile resolve(String s) {
        return Validation.UNIQUEID.matches(s)
                ? MooQueries.getInstance().getPlayerProfile(UUID.fromString(s))
                : MooQueries.getInstance().getPlayerProfile(s);
    }

    @Override
    public Class<PlayerProfile> typeClass() {
        return PlayerProfile.class;
    }

    @Override
    public boolean checkCustom(String arg, String s) {
        return true;
    }

    @Override
    public String handleCustomException(String s) {
        return null;
    }

}
