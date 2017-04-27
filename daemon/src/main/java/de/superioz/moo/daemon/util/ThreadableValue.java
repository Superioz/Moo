package de.superioz.moo.daemon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ThreadableValue<T> {

    private T object;

    public T get(){
        return object;
    }

    public void set(T t){
        this.object = t;
    }

}
