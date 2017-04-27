package de.superioz.moo.api.event;

public interface Responseable<T> {

    void setResponse(T t);

    T getResponse();

}
