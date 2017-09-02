package de.superioz.moo.network.packet;

import de.superioz.moo.api.event.EventEar;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.utils.EventUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class to execute packet events (i/o)
 */
public final class PacketAdapting {

    private static PacketAdapting instance;

    public static synchronized PacketAdapting getInstance(){
        if(instance == null){
            instance = new PacketAdapting();
        }
        return instance;
    }

    /**
     * The executor service to run something in another thread (async)
     */
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    /**
     * Map of registered {@link PacketAdapter}'s but with different values.<br>
     * {@link EventEar} represents the specific listener method/class/whatever
     */
    private final ConcurrentMap<Class<?>, Map<Class<?>, List<EventEar>>> classAdapterMap = new ConcurrentHashMap<>();

    /**
     * Executes given events
     *
     * @param packet The packets
     */
    public void execute(AbstractPacket packet) {
        EventUtil.execute(packet, EXECUTOR_SERVICE, getHandler(packet));
    }

    /**
     * Get handler from given event
     *
     * @param packet The packets
     * @return The list of listener
     */
    public List<EventEar> getHandler(AbstractPacket packet) {
        List<EventEar> listeners = new ArrayList<>();

        for(Map<Class<?>, List<EventEar>> m : classAdapterMap.values()) {
            if(m.containsKey(packet.getClass())) {
                listeners.addAll(m.get(packet.getClass()));
            }
        }
        return listeners;
    }

    /**
     * Registers event listener with adding all to-listen events and the associated listener object
     *
     * @param packetAdapter The packetAdapter classes
     * @return The result
     */
    public int register(PacketAdapter... packetAdapter) {
        int count = 0;

        for(PacketAdapter adapter : packetAdapter) {
            if(!classAdapterMap.containsKey(adapter.getClass())){
                classAdapterMap.put(adapter.getClass(), EventUtil.fetchHandler(adapter, AbstractPacket.class, PacketHandler.class));
                count++;
            }
        }
        return count;
    }

    /**
     * Unregisters an event listener from both maps
     *
     * @param eventListener The event listener
     * @return The result
     */
    public boolean unregister(EventListener eventListener) {
        Class<?> c = eventListener.getClass();
        classAdapterMap.remove(c);

        return classAdapterMap.containsKey(c);
    }

    /**
     * Clears all adapter from the map
     */
    public void unregisterAll() {
        classAdapterMap.clear();
    }

}
