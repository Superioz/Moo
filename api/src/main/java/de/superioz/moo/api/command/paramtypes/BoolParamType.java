package de.superioz.moo.api.command.paramtypes;

import de.superioz.moo.api.command.param.ParamType;

public class BoolParamType extends ParamType<Boolean> {

    @Override
    public String label() {
        return "bool";
    }

    @Override
    public Boolean resolve(String s) {
        return Boolean.valueOf(s);
    }

    @Override
    public Class<Boolean> typeClass() {
        return Boolean.class;
    }

    @Override
    public boolean checkPositivity(String arg) {
        return resolve(arg);
    }

    @Override
    public boolean checkNegativity(String arg) {
        return !resolve(arg);
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
