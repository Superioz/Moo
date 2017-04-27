package de.superioz.moo.api.keyvalue;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class KeyMultiValue extends Keyable {

    private List<Object> values;

    public KeyMultiValue(String key, Object... values) {
        super(key);
        this.values = Arrays.asList(values);
    }

    @Override
    public String toString() {
        return key + ": " + values;
    }
}
