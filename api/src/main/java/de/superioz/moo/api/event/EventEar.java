package de.superioz.moo.api.event;

import de.superioz.moo.api.events.CommandErrorEvent;
import de.superioz.moo.api.utils.ReflectionUtil;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Getter
public class EventEar {

    /**
     * Name of the priority method
     */
    public static final String PRIORITY_METHOD = "priority";

    /**
     * The instance who declared the method
     */
    private Object declaringInstance;

    /**
     * The class of the event type (e.g. {@link CommandErrorEvent})
     */
    private Class<?> eventType;

    /**
     * The class of the event
     */
    private Class<?> eventClass;

    /**
     * The method
     */
    private Method method;

    /**
     * The event priority
     */
    private EventPriority priority;

    public EventEar(Object declaringInstance, Class<?> eventType, Class<?> eventClass, Method method) {
        this.declaringInstance = declaringInstance;
        this.eventType = eventType;
        this.eventClass = eventClass;
        this.method = method;

        for(Annotation a : method.getDeclaredAnnotations()) {
            for(Method m : a.getClass().getMethods()) {
                if(m.getName().equals(PRIORITY_METHOD)) {
                    priority = (EventPriority) ReflectionUtil.invokeMethod(m, a);
                }
            }
        }
    }

}
