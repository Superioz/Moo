package de.superioz.moo.api.command.paramtypes;

import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.util.Validation;

public class IntegerParamType extends ParamType<Integer> {

    @Override
    public String label() {
        return "int";
    }

    @Override
    public Integer resolve(String s) {
        return Validation.INTEGER.matches(s) ? Integer.valueOf(s) : null;
    }

    @Override
    public Class<Integer> typeClass() {
        return Integer.class;
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
