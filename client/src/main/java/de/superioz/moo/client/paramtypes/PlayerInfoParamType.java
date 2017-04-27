package de.superioz.moo.client.paramtypes;

import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.common.PlayerInfo;
import de.superioz.moo.api.util.Validation;
import de.superioz.moo.client.common.MooQueries;

import java.util.UUID;

public class PlayerInfoParamType extends ParamType<PlayerInfo> {

    @Override
    public String label() {
        return "playerinfo";
    }

    @Override
    public PlayerInfo resolve(String s) {
        return Validation.UNIQUEID.matches(s)
                ? MooQueries.getInstance().getPlayerInfo(UUID.fromString(s))
                : MooQueries.getInstance().getPlayerInfo(s);
    }

    @Override
    public Class<PlayerInfo> typeClass() {
        return PlayerInfo.class;
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
