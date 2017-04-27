package de.superioz.moo.client.common;

import de.superioz.moo.client.Moo;
import lombok.Getter;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.protocol.packet.PacketAdapter;
import de.superioz.moo.protocol.packet.PacketAdapting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class MooPluginStartup {

    /**
     * Registered commands
     */
    private final List<Object> commands = new ArrayList<>();

    /**
     * Registered listeners
     */
    private final List<Object> leftOvers = new ArrayList<>();
    private final List<EventListener> listeners = new ArrayList<>();
    private final List<PacketAdapter> packetAdapters = new ArrayList<>();

    /**
     * Registered types
     */
    private final List<ParamType> paramTypes = new ArrayList<>();

    public void registerCommands(Object... commandClasses) {
        this.commands.addAll(Arrays.asList(commandClasses));
    }

    public void registerListeners(Object... listenerClasses) {
        for(Object listenerClass : listenerClasses) {
            if(listenerClass instanceof EventListener) {
                listeners.add((EventListener) listenerClass);
            }
            if(listenerClass instanceof PacketAdapter) {
                packetAdapters.add((PacketAdapter) listenerClass);
            }
            leftOvers.add(listenerClass);
        }
    }

    public void registerTypes(ParamType... types) {
        paramTypes.addAll(Arrays.asList(types));
    }

    /**
     * Executes the registering of the objects
     */
    public void execute(MooPlugin plugin) {
        int count;

        // register event listeners
        count = EventExecutor.getInstance().register(listeners.toArray(new EventListener[]{}));

        // register left overs
        for(Object leftOver : leftOvers) {
            if(plugin.registerLeftOvers().apply(leftOver)) {
                count++;
            }
        }
        Moo.getInstance().getLogger().info("Registered listeners. (" + count + ")");

        // register packet adapter
        count = PacketAdapting.getInstance().register(packetAdapters.toArray(new PacketAdapter[]{}));
        Moo.getInstance().getLogger().info("Registered packet adapter. (" + count + ")");

        // register commands
        for(Object command : commands) {
            count += CommandRegistry.getInstance().registerCommands(command);
        }
        Moo.getInstance().getLogger().info("Registered commands. (" + count + ")");

        // register param types
        CommandRegistry.getInstance().getParamTypeRegistry().register(paramTypes.toArray(new ParamType[]{}));
        Moo.getInstance().getLogger().info("Registered param types. (" + paramTypes.size() + ")");
    }


}
