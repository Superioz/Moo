package de.superioz.moo.api.keyvalue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TypeableKey {

    /**
     * The key of the {@link #valueClass}
     */
    private String key;

    /**
     * The value class
     */
    private Class<?> valueClass;

}

