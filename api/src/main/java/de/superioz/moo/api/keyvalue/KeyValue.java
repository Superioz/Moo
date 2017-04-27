package de.superioz.moo.api.keyvalue;

import lombok.Getter;

@Getter
public class KeyValue extends Keyable {

    private Object val;

    public KeyValue(String key, Object val) {
        super(key);
        this.val = val;
    }
}
