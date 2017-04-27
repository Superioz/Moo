package de.superioz.moo.api.reaction;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.util.Procedure;
import de.superioz.moo.api.utils.NumberUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class provides another way of checking some type with following reaction.<br>
 * Example: You received an integer and you want to use either if-else or switch-case but this nested style
 * makes the code somehow confusing. And if you want to edit asynchronous cases makes it even more confusing. And that is the usage for this class.
 */
public class Reaction {

    public static final Integer ASYNC = 1;

    public static final Integer INVERTED = 0;
    public static final Integer GREATER_THAN = 2;
    public static final Integer GREATER_THAN_EQUALS = 3;
    public static final Integer LOWER_THAN = 4;
    public static final Integer LOWER_THAN_EQUALS = 5;

    /**
     * Executor service for executing reactions async
     */
    private static ExecutorService executors = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("reaction-thread-%d").build());

    /**
     * Reacts to the condition if the condition is true
     *
     * @param condition The condition
     * @param procedure The procedure after validation
     * @return Exception or not
     */
    public static boolean react(boolean condition, Procedure procedure, Integer... flags) {
        if(condition) {
            List<Integer> flagList = Arrays.asList(flags);

            try {
                if(flagList.contains(ASYNC)) {
                    executors.execute(procedure::invoke);
                }
                else {
                    procedure.invoke();
                }
            }
            catch(Exception e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reacts to given condition by using given reactors. That makes it easier to react to different states,
     * otherwise you would have to react to every state seperately.
     *
     * @param object   The object to be compared with the object from the reactors
     * @param reactors The reactors to react to different states of the object
     * @return If the reaction is cancelled
     * @see Reactor
     */
    public static <T> boolean react(T object, Reactor<T>... reactors) {
        boolean cancelled = false;
        for(Reactor reactor : reactors) {
            cancelled = !react(reactor.getObject(), object, reactor::invoke) || reactor.isCancelled();
        }
        return cancelled;
    }

    public static <T> boolean reactAsync(T object, Reactor<T>... reactors) {
        try {
            executors.execute(() -> react(object, reactors));
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Reacts to the condition if object1 is equals object2
     *
     * @param o1        The first object
     * @param o2        The second object
     * @param procedure The reaction
     * @param flags     Flags for meta data (async, ..)
     * @return Exception or not
     */
    public static boolean react(Object o1, Object o2, Procedure procedure, Integer... flags) {
        List<Integer> flagList = Arrays.asList(flags);
        boolean condition = flagList.isEmpty() && (o1.equals(o2) || o1 == null);

        for(Integer i : flagList){
            if(i.equals(INVERTED)){
                condition = !o1.equals(o2);
            }
            else if(i.equals(GREATER_THAN)){
                condition = NumberUtil.checkPosition(o1, o2, true, false);
            }
            else if(i.equals(GREATER_THAN_EQUALS)){
                condition = NumberUtil.checkPosition(o1, o2, true, true);
            }
            else if(i.equals(LOWER_THAN)){
                condition = NumberUtil.checkPosition(o1, o2, false, false);
            }
            else if(i.equals(LOWER_THAN_EQUALS)){
                condition = NumberUtil.checkPosition(o1, o2, false, true);
            }
        }
        return react(condition, procedure, flags);
    }
}
