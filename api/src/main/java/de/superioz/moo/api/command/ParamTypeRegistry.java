package de.superioz.moo.api.command;

import de.superioz.moo.api.collection.Registry;
import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.command.paramtypes.DoubleParamType;
import de.superioz.moo.api.command.paramtypes.IntegerParamType;
import de.superioz.moo.api.command.paramtypes.LongParamType;
import de.superioz.moo.api.command.paramtypes.BoolParamType;

public class ParamTypeRegistry extends Registry<String, ParamType> {

    /**
     * Default param definitions
     */
    private static final ParamType[] DEFAULT_PARAM_TYPES = new ParamType[]{
            new BoolParamType(), new DoubleParamType(), new IntegerParamType(), new LongParamType()
    };

    public ParamTypeRegistry() {
        register(DEFAULT_PARAM_TYPES);
    }

    public ParamType get(Class<?> c) {
        for(Object t : keyObjectMap.values()) {
            if(ParamType.class.isAssignableFrom(t.getClass())) {
                ParamType type = (ParamType) t;
                if(type.typeClass().equals(c)) return type;
            }
        }
        return null;
    }

    /**
     * Registers a param definition type
     *
     * @param paramDefTypes The type objects
     */
    @Override
    public boolean register(ParamType... paramDefTypes) {
        boolean r = false;
        for(ParamType t : paramDefTypes) {
            r = register(t.label(), t);
        }
        return r;
    }

    /**
     * Unregisters a param definition type
     *
     * @param paramDefTypes The type objects
     */
    @Override
    public boolean unregister(ParamType... paramDefTypes) {
        boolean r = false;
        for(ParamType t : paramDefTypes) {
            r = unregister(t.label(), t);
        }
        return r;
    }

}
