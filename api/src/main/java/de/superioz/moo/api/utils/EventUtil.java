package de.superioz.moo.api.utils;

import de.superioz.moo.api.event.EventEar;
import de.superioz.moo.api.common.RunAsynchronous;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public final class EventUtil {

    /**
     * Executes an event with given {@link EventEar}'s<br>
     * The executor service is the service to use if {@link RunAsynchronous} is found<br>
     * This method returns a {@link Future}, but this is only necessary if you want to
     * determine when every listener instance is finished
     *
     * @param parameter       The event parameter
     * @param executorService The executor service
     * @param ears            The ears
     */
    public static <E> void execute(E parameter, ExecutorService executorService, List<EventEar> ears) {
        // sorting
        ears.sort((o1, o2) -> ((Integer) o2.getPriority().getValue()).compareTo(o1.getPriority().getValue()));

        // loop through every event listener (ear)
        for(EventEar eventEar : ears) {
            if(eventEar.getMethod().getDeclaringClass().isAnnotationPresent(RunAsynchronous.class)
                    || eventEar.getMethod().isAnnotationPresent(RunAsynchronous.class)) {

                executorService.execute(() -> simpleExecute(parameter, eventEar));
            }
            else {
                simpleExecute(parameter, eventEar);
            }
            /*if(f) count++;*/
        }
    }

    /**
     * Simply executes given ear with parameter
     *
     * @param parameter The parameter
     * @param eventEar  The ear
     * @return The result
     */
    private static boolean simpleExecute(Object parameter, EventEar eventEar) {
        try {
            eventEar.getMethod().invoke(eventEar.getDeclaringInstance(), parameter);
            return true;
        }
        catch(Exception e) {
            System.err.println("Could not invoke listener method! Thread: ");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get all handlers from given class to execute a event's class with
     *
     * @param instance          The instance of the class
     * @param parameterClass    The parameter class (parameter of every listener method)
     * @param neededAnnotations The annotations that needs to be set
     * @param <A>               Just the type of an annotation
     * @return The handlers as map
     */
    public static <A extends Annotation> Map<Class<?>, List<EventEar>> fetchHandler(Object instance, Class<?> parameterClass,
                                                                                    Class<A>... neededAnnotations) {
        Class<?> c = instance.getClass();
        Map<Class<?>, List<EventEar>> eventMap = new HashMap<>();
        if(c == null || parameterClass == null) return eventMap;

        for(Method m : c.getMethods()) {
            for(Class<? extends Annotation> aC : neededAnnotations) {
                if(!m.isAnnotationPresent(aC)) break;
            }
            Class<?>[] parameters = m.getParameterTypes();

            if((!m.getReturnType().equals(void.class)
                    || !(parameters.length == 1 && parameterClass.isAssignableFrom(parameters[0])))) continue;
            Class<?> eventClass = parameters[0];
            List<EventEar> eventMethods = eventMap.get(eventClass);
            if(eventMethods == null) eventMethods = new ArrayList<>();
            eventMethods.add(new EventEar(instance, parameterClass, eventClass, m));

            eventMap.put(eventClass, eventMethods);
        }
        return eventMap;
    }

}
