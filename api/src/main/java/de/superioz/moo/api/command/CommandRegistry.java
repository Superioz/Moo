package de.superioz.moo.api.command;

import de.superioz.moo.api.collection.Registry;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.param.ParamType;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.events.CommandErrorEvent;
import de.superioz.moo.api.events.CommandHelpEvent;
import de.superioz.moo.api.events.CommandRegisterEvent;
import de.superioz.moo.api.events.TabCompleteEvent;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import lombok.Getter;
import de.superioz.moo.api.command.help.ArgumentHelp;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class registers and stores every command and the parameter definitions<br>
 * DO NOT forget to register following events somehow:<br>
 * {@link CommandErrorEvent}<br>
 * {@link TabCompleteEvent}<br>
 * {@link CommandHelpEvent}
 *
 * @see ParamType
 * @see CommandEventAdapter
 */
public class CommandRegistry extends Registry<String, CommandInstance> {

    private static CommandRegistry instance;

    public static CommandRegistry getInstance() {
        if(instance == null) {
            instance = new CommandRegistry();
        }
        return instance;
    }

    @Getter
    private final ParamTypeRegistry paramTypeRegistry = new ParamTypeRegistry();

    private CommandRegistry() {

    }

    /**
     * Registers an eventAdapter which means registering its events
     *
     * @param eventAdapter The event adapter
     * @param <T>          The type
     */
    public <T> void registerEventAdapter(CommandEventAdapter<T> eventAdapter) {
        EventExecutor.getInstance().register(eventAdapter);
    }

    /**
     * Executes a command
     *
     * @param args    The arguments
     * @param context The context
     * @param <T>     The type
     */
    public <T> boolean executeCommand(String[] args, CommandContext<T> context) {
        if(args.length == 0) return false;
        String cmd = args[0];
        String[] parameter = Arrays.copyOfRange(args, 1, args.length);

        CommandInstance command = getCommand(cmd);
        return command != null && command.execute(context, parameter);
    }

    public <T> boolean executeCommand(String cmd, String[] args, CommandContext<T> context) {
        List<String> l = new ArrayList<>();
        l.add(cmd);
        l.addAll(Arrays.asList(args));
        return executeCommand(l.toArray(new String[l.size()]), context);
    }

    public boolean executeCommand(String[] args) {
        return executeCommand(args, CommandContext.DEFAULT);
    }

    /**
     * Gets the command with given label
     *
     * @param label The label
     * @return The command
     */
    public CommandInstance getCommand(String label) {
        if(label.isEmpty()) return null;
        for(CommandInstance instance : getRegisteredCommands().values()) {
            if(instance.getLabel().equalsIgnoreCase(label)
                    || instance.getAliases().contains(label.toLowerCase())) return instance;
        }
        return null;
    }

    /**
     * Gets the most similar command from given label
     *
     * @param label The label
     * @return The command instance
     */
    public CommandInstance getSimilarCommand(String label) {
        if(label.isEmpty()) return null;
        List<String> commands = getCommands();
        Map.Entry<String, Double> entry = StringUtil.getMostSimilar(label, commands, 1, true);
        if(entry == null) {
            return null;
        }
        String mostSimilar = entry.getKey();
        return getCommand(mostSimilar);
    }

    /**
     * Checks if the registry contains a command with given label
     *
     * @param label The label
     * @return The result
     */
    public boolean hasCommand(String label) {
        return getCommand(label) != null;
    }

    /**
     * Get all registered commands
     *
     * @return The map of commands
     */
    public Map<String, CommandInstance> getRegisteredCommands() {
        return keyObjectMap;
    }

    /**
     * Get the commands labels
     *
     * @return The commands labels
     */
    public List<String> getCommands() {
        return new ArrayList<>(keyObjectMap.keySet());
    }

    /**
     * Get the commands with the command type root
     *
     * @return The list of commands
     */
    public List<CommandInstance> getRootCommands() {
        return getRegisteredCommands().values()
                .parallelStream()
                .filter(instance -> instance.getCommandType() == CommandType.ROOT)
                .collect(Collectors.toList());
    }

    /**
     * Clears all maps
     */
    public void unregisterAll() {
        keyObjectMap.clear();
    }

    /**
     * Register commands from given classes
     *
     * @param classes The classes
     */
    public int registerCommands(Object... classes) {
        int count = 0;
        List<CommandInstance> roots = Fetcher.fetchCommands(classes);

        for(CommandInstance root : roots) {
            if(root != null && !keyObjectMap.containsKey(root.getLabel())) {
                register(root.getLabel(), root);
                EventExecutor.getInstance().execute(new CommandRegisterEvent(root));

                count++;
            }
        }
        return count;
    }

    public int registerCommandsSeperately(Object... classes) {
        int i = 0;
        for(Object aClass : classes) {
            i += registerCommands(aClass);
        }
        return i;
    }

    public static class Fetcher {

        /**
         * Fetches the root command and all the other commands from given classes
         *
         * @param objects The classe's objects
         * @return The root command (If no one exists return null)
         */
        private static List<CommandInstance> fetchCommands(Object... objects) {
            Map<String, CommandInstance> commands = new HashMap<>();
            Map<Method, Object> commandMethods = new HashMap<>();
            List<CommandInstance> roots = new ArrayList<>();

            // list all methods that are commandable
            for(Object o : objects) {
                for(Method m : o.getClass().getDeclaredMethods()) {
                    if(!checkMethod(m)) continue;
                    commandMethods.put(m, o);
                }
            }

            // list all commands
            for(Method m : commandMethods.keySet()) {
                CommandInstance instance = new CommandInstance(commandMethods.get(m), m);
                if(instance.getCommandType() == CommandType.ROOT) roots.add(instance);

                commands.put(instance.getLabel(), instance);
            }

            for(CommandInstance root : roots) {
                initRelations(root, commands);
            }
            Map<String, CommandInstance> newCommands = new HashMap<>();
            commands.forEach((s, commandInstance) -> newCommands.put(commandInstance.getPath(), commandInstance));

            initSpecialMethods(newCommands, objects);
            return roots;
        }

        /**
         * Initialises the relationships between the root command and the other commands
         *
         * @param root     The root command
         * @param commands The commands
         */
        private static void initRelations(CommandInstance root, Map<String, CommandInstance> commands) {
            for(CommandInstance cmd : commands.values()) {
                CommandInstance p = commands.get(cmd.getParentName());
                if(p != null) {
                    if(cmd.getParent() == null) cmd.setParent(p);
                    if(cmd.getRoot() == null) cmd.setRoot(root);
                    if(!p.getChildrens().contains(cmd)) p.addChildren(cmd);
                }
            }
            commands.values().forEach(CommandInstance::initTreePath);
        }

        /**
         * Initialises the tabCompletion for given commands with classes
         * So it will fetch all methods and check for occurences
         *
         * @param commands The commands
         * @param objects  The classes object's
         */
        private static void initSpecialMethods(Map<String, CommandInstance> commands, Object... objects) {
            // list all methods that are tabcompletable
            for(Object o : objects) {
                for(Method m : o.getClass().getDeclaredMethods()) {
                    if(checkTabCompleteMethod(m)) {
                        for(CommandInstance cmd : commands.values()) {
                            if(cmd.getCommandType() == CommandType.ROOT) {
                                cmd.addTabCompletion(o, m);
                            }
                        }
                    }
                    else if(checkArgumentHelperMethod(m)) {
                        for(CommandInstance cmd : commands.values()) {
                            if(cmd.getCommandType() == CommandType.ROOT) {
                                cmd.addArgumentHelper(o, m);
                            }
                        }
                    }
                }
            }
        }

        /**
         * Checks if the method is commandable
         *
         * @param m The to-check method
         * @return The result
         */
        public static boolean checkMethod(Method m) {
            return ReflectionUtil.checkMethod(m, Command.class, new Class<?>[]{CommandContext.class, ParamSet.class});
        }

        /**
         * Checks if the method is tabcompletable
         *
         * @param m The method
         * @return The result
         */
        public static boolean checkTabCompleteMethod(Method m) {
            return ReflectionUtil.checkMethod(m, TabCompletion.class, new Class<?>[]{TabCompletor.class});
        }

        /**
         * Checks if the method is argumenthelpable
         *
         * @param m The method
         * @return The result
         */
        public static boolean checkArgumentHelperMethod(Method m) {
            return ReflectionUtil.checkMethod(m, ArgumentHelp.class, new Class<?>[]{ArgumentHelper.class});
        }

    }

}
