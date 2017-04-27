package de.superioz.moo.api.keyvalue;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class FinalValue<T> {

    private T value;

    public T get(){
        return value;
    }

    public void set(T t){
        value = t;
    }

}
