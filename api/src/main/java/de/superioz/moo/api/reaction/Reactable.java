package de.superioz.moo.api.reaction;

import lombok.Getter;
import de.superioz.moo.api.util.Procedure;

@Getter
public abstract class Reactable<E> {

    protected String key;
    protected int id;
    protected E element;

    /**
     * Checks if key is one of the given keys
     *
     * @param keys The keys (multiple or none)
     * @return The result
     */
    protected boolean checkKey(String... keys) {
        if(keys.length != 0) {
            for(String s : keys) {
                if(key.equalsIgnoreCase(s) || key.endsWith(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Executes given procedure if the id=getId and if the key=getKey
     *
     * @param id        The id of the procedure
     * @param procedure The procedure itself
     * @param keys      The keys (only one of them must be correct)
     */
    public void react(int id, Procedure procedure, String... keys) {
        if((keys.length == 0 || checkKey(keys)) && id == getId()) {
            procedure.invoke();
        }
    }

    public void react(int id, E e, String... keys) {
        react(id, () -> Reactable.this.element = e, keys);
    }

    public void react(int id, E e) {
        react(id, e, new String[]{});
    }

}
