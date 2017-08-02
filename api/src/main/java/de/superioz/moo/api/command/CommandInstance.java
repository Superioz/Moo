package de.superioz.moo.api.command;

import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.command.help.ArgumentHelp;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.command.tabcomplete.TabCompletion;
import de.superioz.moo.api.command.tabcomplete.TabCompletor;
import de.superioz.moo.api.common.RunAsynchronous;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.events.CommandErrorEvent;
import de.superioz.moo.api.events.CommandHelpEvent;
import de.superioz.moo.api.events.PreCommandEvent;
import de.superioz.moo.api.utils.ReflectionUtil;
import de.superioz.moo.api.utils.StringUtil;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

/**
 * A wrapper class of {@link Command} and the class which executes the command itself
 */
@Getter
public class CommandInstance {

    /**
     * The help flag
     */
    public static final String HELP_FLAG = "?";

    /**
     * The path indicator/seperator
     */
    public static final String PATH = ".";

    private String label;
    private String parentName;
    private List<String> aliases;
    private String description;
    private String permission;
    private List<String> flags;
    private List<CommandFlag> flagBases = new ArrayList<>();
    private CommandUsage usage;
    private AllowedCommandSender commandTarget;

    /**
     * The label of the command (e.g.: command.subcommand.subsubcommand)
     */
    private String path;

    /**
     * Type of the command (e.g. Root or Sub)
     */
    private CommandType commandType;

    /**
     * If the command is not the root command than would be this value != null
     */
    @Setter
    private CommandInstance root;

    /**
     * If the command is a subCommand this parent is the parent command (otherwise null)
     */
    @Setter
    private CommandInstance parent;

    /**
     * Children of this command (if available)
     */
    private Map<String, CommandInstance> children = new HashMap<>();

    /**
     * The class object of the class where the command method is initialised
     */
    private Object methodClassObject;

    /**
     * The method itself
     *
     * @see #invokeMethod(CommandContext, ParamSet)
     */
    private Method method;

    /**
     * Tab completor objects. Method is the tabCompletion method and the object the declared
     * class object
     *
     * @see TabCompletion
     */
    private Map<Method, Object> tabCompletionMap = new HashMap<>();

    /**
     * Argument helper methods. These methods can be executed to send information about one argument and its usage
     *
     * @see ArgumentHelp
     */
    private Map<Method, Object> argumentHelperMap = new HashMap<>();

    public CommandInstance(Object methodClassObject, Method method) {
        this.methodClassObject = methodClassObject;
        this.method = method;
        if(!method.isAnnotationPresent(Command.class)) return;

        Command command = method.getAnnotation(Command.class);
        this.label = command.label();
        this.parentName = command.parent();
        this.aliases = StringUtil.modifyStringList(Arrays.asList(command.aliases()), String::toLowerCase);
        this.description = command.desc();
        this.permission = command.permission();
        this.flags = Arrays.asList(command.flags());
        this.usage = new CommandUsage(command.usage());
        this.commandTarget = command.commandTarget();

        // remove empties
        this.aliases = StringUtil.removeEmpties(aliases);
        this.flags = StringUtil.removeEmpties(flags);
        this.flagBases = clearFlags();

        this.commandType = parentName.isEmpty() ? CommandType.ROOT : CommandType.SUB;
    }

    /**
     * Initialises the {@link #path}
     */
    public void initTreePath() {
        if(this.path != null) return;
        StringBuilder path = new StringBuilder(label);

        CommandInstance current = this;
        CommandInstance parent;
        while((parent = current.getParent()) != null){
            current = parent;
            path.insert(0, parent.getLabel() + PATH);
        }
        if(path == null) path = new StringBuilder();
        this.path = path.toString();
    }

    /**
     * Executes the tabcompletion for given tab completor
     *
     * @param tabCompletor The tab completor
     */
    public void executeTabCompletion(TabCompletor tabCompletor) {
        CommandInstance root = getRoot();
        Map<Method, Object> map = root == null ? tabCompletionMap : root.getTabCompletionMap();

        for(Method m : map.keySet()) {
            ReflectionUtil.invokeMethod(m, map.get(m), tabCompletor);
        }
    }

    /**
     * Executes the argument helper for given helper
     *
     * @param helper The argument helper
     */
    public void executeArgumentHelper(ArgumentHelper helper) {
        CommandInstance root = getRoot();
        Map<Method, Object> map = root == null ? argumentHelperMap : root.getArgumentHelperMap();

        for(Method m : map.keySet()) {
            ReflectionUtil.invokeMethod(m, map.get(m), helper);
        }
    }

    /**
     * Adds a tabcompletion to the map of tabCompletions
     *
     * @param tabCompletionMethod The tabCompletion method
     */
    public void addTabCompletion(Object instance, Method tabCompletionMethod) {
        if(tabCompletionMethod == null) return;
        this.tabCompletionMap.put(tabCompletionMethod, instance);
    }

    /**
     * Adds an argument helper method to the list
     *
     * @param method The method
     */
    public void addArgumentHelper(Object instance, Method method) {
        this.argumentHelperMap.put(method, instance);
    }

    /**
     * Checks if the label starts with a slash. That means
     * it's a command only for admins (Example: /fly and //fly to differentiate them)
     *
     * @return The result
     */
    public boolean isAdminCommand() {
        return label.startsWith("/");
    }

    /**
     * Checks if the method is running async
     *
     * @return The result
     */
    public boolean isRunningAsync() {
        return getMethod().isAnnotationPresent(RunAsynchronous.class);
    }

    /**
     * Gets a list of the path
     *
     * @return The list
     */
    public List<String> getWholePath() {
        return Arrays.asList(path.split("\\."));
    }

    /**
     * Gets the path before this command (all parents)
     *
     * @return The list
     */
    public List<String> getBeforePath() {
        List<String> whole = getWholePath();
        return whole.size() == 1 ? new ArrayList<>() : whole.subList(0, whole.size() - 1);
    }

    /**
     * Clears the flags by removing the descriptor and creating an initialised flag list
     *
     * @return The flag list
     */
    private List<CommandFlag> clearFlags() {
        List<String> newFlags = new ArrayList<>();
        List<CommandFlag> initialisedFlags = new ArrayList<>();

        for(String s : flags) {
            String[] split = s.split("\\[", 2);

            String desc = split.length > 1 ? split[1].replace("]", "") : "";
            String label = split[0];
            newFlags.add(label);
            initialisedFlags.add(new CommandFlag(label, desc));
        }
        this.flags = newFlags;
        return initialisedFlags;
    }

    /**
     * Gets the flag base of given label flag
     *
     * @param label The label of the flag
     * @return The flag base
     */
    public CommandFlag getFlagBase(String label) {
        if(label.equals(HELP_FLAG)) return new CommandFlag(label, "");
        for(CommandFlag f : flagBases) {
            if(f.getLabel().equals(label)) {
                return f;
            }
            /*if(s.startsWith(label)) {
                String[] split = s.split("\\[", 2);

                String desc = split.length > 1 ? split[1].replace("]", "") : "";
                label = split[0];
                flag = new CommandFlag(label, desc);
                break;
            }*/
        }
        return null;
    }

    /**
     * Checks if the command has a parent
     *
     * @return The result
     */
    public boolean hasParent() {
        return getParent() != null;
    }

    /**
     * Checks if this command has childrens
     *
     * @return The result
     */
    public boolean hasChildren() {
        return getChildrens().size() != 0;
    }

    /**
     * Adds a children to the children list
     *
     * @param command The command
     */
    public void addChildren(CommandInstance command) {
        this.children.put(command.getLabel(), command);
    }

    /**
     * Get the list of children
     *
     * @return The list
     */
    public List<CommandInstance> getChildrens() {
        return new ArrayList<>(children.values());
    }

    public CommandInstance getChildren(String label) {
        for(CommandInstance ci : getChildrens()) {
            if(ci.getLabel().equalsIgnoreCase(label)
                    || ci.getAliases().contains(label.toLowerCase())) return ci;
        }
        return null;
    }

    /**
     * Get the children map
     *
     * @return The map
     */
    public Map<String, CommandInstance> getChildrenMap() {
        return children;
    }

    public Pair<CommandInstance, String[]> getInstance(String[] args, Function<CommandInstance, Boolean> preCommand) {
        if(preCommand != null && !preCommand.apply(this)) {
            return null;
        }

        // check for sub command
        if(args.length != 0 && getChildrenMap().containsKey(args[0])) {
            CommandInstance children = getChildrenMap().get(args[0]);

            return children.getInstance(Arrays.copyOfRange(args, 1, args.length), preCommand);
        }
        return new Pair<>(this, args);
    }

    /**
     * Executes this command
     *
     * @param context The command context
     * @param args    The arguments
     * @param <T>     The type
     * @return The result
     */
    public <T> boolean execute(CommandContext<T> context, String[] args) {
        // get command instance atg last argument (subcommands)
        final PreCommandEvent[] event = new PreCommandEvent[1];
        Pair<CommandInstance, String[]> command = getInstance(args, commandInstance -> {
            event[0] = new PreCommandEvent<>(context, commandInstance);
            EventExecutor.getInstance().execute(event[0]);
            return !event[0].isCancelled();
        });
        if(command == null) {
            return false;
        }
        CommandInstance instance = command.getKey();
        context.setCommand(instance);

        // set parameter set
        ParamSet parameterSet = new ParamSet(instance, command.getValue());
        context.setParamSet(parameterSet);

        // check for command help
        if(parameterSet.hasFlag(HELP_FLAG)) {
            if(!getFlags().contains(HELP_FLAG)) {
                // the user wants to auto handle the help flag
                CommandHelpEvent helpEvent = new CommandHelpEvent(context);
                EventExecutor.getInstance().execute(helpEvent);

                if(helpEvent.isCancelled()) {
                    return true;
                }
            }
            // the user wants to custom handle the help flag
        }

        // check length of arguments
        if(parameterSet.size() < context.getCommand().getUsage().getNeededSize()) {
            context.invalidUsage();
            return false;
        }

        // execute command
        if(event[0].getService() != null) {
            event[0].getService().execute(() -> invokeMethod(context, parameterSet));
        }
        else {
            return invokeMethod(context, parameterSet);
        }
        return true;
    }

    /**
     * Executes the method
     *
     * @param context  The context
     * @param paramSet The parameterSet
     * @param <T>      The type
     * @return The result
     */
    private <T> boolean invokeMethod(CommandContext<T> context, ParamSet paramSet) {
        try {
            CommandInstance command = context.getCommand();
            command.getMethod().invoke(command.getMethodClassObject(), context, paramSet);
        }
        catch(Exception e) {
            EventExecutor.getInstance().execute(new CommandErrorEvent<>(context, this, e));
            return false;
        }
        return true;
    }

}
