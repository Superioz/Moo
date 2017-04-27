package de.superioz.moo.api.keyvalue;

import lombok.Getter;

@Getter
public class Triple<A, B, C> {

    private A a;
    private B b;
    private C c;

    public Triple(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }
}
