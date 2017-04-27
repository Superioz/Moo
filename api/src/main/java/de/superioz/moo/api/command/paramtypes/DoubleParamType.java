package de.superioz.moo.api.command.paramtypes;

import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.util.Validation;

public class DoubleParamType extends ParamType<Double> {

    @Override
    public String label() {
        return "double";
    }

    @Override
    public Double resolve(String s) {
        if(Validation.INTEGER.matches(s)) return (double)Integer.valueOf(s);
        if(Validation.LONG.matches(s)) return (double)Long.valueOf(s);
        return Validation.DOUBLE.matches(s) ? Double.valueOf(s) : null;
    }

    @Override
    public Class<Double> typeClass() {
        return Double.class;
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
