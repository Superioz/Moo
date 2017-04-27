package de.superioz.moo.client.common;

import de.superioz.moo.api.collection.MultiRegistry;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.event.EventListener;

public class PluginStartupRegistry extends MultiRegistry<String, Object> {

    public static final String COMMANDS = "commands";
    public static final String LISTENERS = "listeners";
    public static final String PARAM_DEFS = "param-defs";

    /**
     * Register commands
     *
     * @param objects The command classes
     * @return The result
     * @see CommandInstance
     */
    public boolean registerCommands(Object... objects) {
        return register(COMMANDS, objects);
    }

    /**
     * Register listeners
     *
     * @param objects The listener classes
     * @return The result
     * @see EventListener
     */
    public boolean registerListeners(Object... objects) {
        return register(LISTENERS, objects);
    }

    /**
     * Register param defs
     *
     * @param paramDefs The param defs class
     * @return The result
     * @see ParamType
     */
    public boolean registerParamDefs(ParamType... paramDefs) {
        return register(PARAM_DEFS, (Object[]) paramDefs);
    }

}
