package de.superioz.moo.api.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import de.superioz.moo.api.utils.EventUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventExecutor implements EventListener {

    private static EventExecutor instance;

    public static synchronized EventExecutor getInstance() {
        if(instance == null) {
            instance = new EventExecutor();
        }
        return instance;
    }

    /**
     * The executor service to run something async
     */
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    /**
     * Map of listeners
     */
    private ConcurrentMap<Class<?>, Map<Class<?>, List<EventEar>>> classListenerMap = new ConcurrentHashMap<>();

    /**
     * Executes given events
     *
     * @param event The events
     */
    public synchronized <E extends Event> boolean execute(E event) {
        EventUtil.execute(event, EXECUTOR_SERVICE, getHandler(event));
        return !(event instanceof Cancellable) || !((Cancellable) event).isCancelled();
    }

    /**
     * Get handler from given event
     *
     * @param event The event
     * @return The list of listener
     */
    public List<EventEar> getHandler(Event event) {
        List<EventEar> listeners = new ArrayList<>();

        for(Map<Class<?>, List<EventEar>> m : classListenerMap.values()) {
            if(m.containsKey(event.getClass())) {
                listeners.addAll(m.get(event.getClass()));
            }
        }
        return listeners;
    }

    /**
     * Registers an event listener with adding all to-listen events and the associated listener object
     *
     * @param eventListeners The eventListener classes
     * @return The result
     */
    public int register(EventListener... eventListeners) {
        int result = 0;
        for(EventListener listener : eventListeners) {
            classListenerMap.put(listener.getClass(), EventUtil.fetchHandler(listener, Event.class, EventHandler.class));
            if(classListenerMap.containsKey(listener.getClass())) result++;
        }
        return result;
    }

    /**
     * Unregisters an event listener from both maps
     *
     * @param eventListener The event listener
     * @return The result
     */
    public boolean unregister(EventListener eventListener) {
        Class<?> c = eventListener.getClass();
        classListenerMap.remove(c);

        return classListenerMap.containsKey(c);
    }

    /**
     * Clears all maps
     */
    public void unregisterAll() {
        classListenerMap.clear();
    }

}
