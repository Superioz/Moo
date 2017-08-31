package de.superioz.moo.api.common;

import lombok.Getter;

public class Replacor<T> {

    @Getter
    private Object[] replacements;

    private T t;

    public Replacor(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    public Replacor accept(Object... replacements) {
        this.replacements = replacements;
        return this;
    }

}
