package de.superioz.moo.api.util;

import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;

public abstract class SimpleSerializable {

    @Override
    public String toString() {
        return ReflectionUtil.serialize(StringUtil.SEPERATOR_2, this);
    }

}
