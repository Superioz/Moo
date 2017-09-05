package de.superioz.moo.api.command.context;

import de.superioz.moo.api.collection.MultiCache;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.help.ArgumentHelper;
import de.superioz.moo.api.command.param.ParamSet;
import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.events.CommandErrorEvent;
import de.superioz.moo.api.exceptions.InvalidArgumentException;
import de.superioz.moo.api.exceptions.InvalidCommandUsageException;
import de.superioz.moo.api.io.LanguageManager;
import de.superioz.moo.api.util.MessageFormatSender;
import lombok.Getter;
import lombok.Setter;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The context of the command means a wrapper for the command itself, the command sender and the
 * arguments
 *
 * @param <T> The type of commandSender (different for bungee/spigot)
 */
@Getter
public abstract class CommandContext<T> {

    /**
     * The name of the console
     */
    public static final String CONSOLE_NAME = ".CONSOLE";

    /**
     * The unique id if the commandSender is the console
     */
    public static final UUID CONSOLE_UUID = UUID.nameUUIDFromBytes(CONSOLE_NAME.getBytes());

    /**
     * The default command context
     */
    public static final CommandContext DEFAULT = new CommandContext(null) {
        @Override
        protected UUID getSendersUniqueId() {
            return CONSOLE_UUID;
        }

        @Override
        protected void message(String msg, Object target) {
            System.out.println(msg);
        }
    };

    /**
     * This cache is for storing values during executing commands
     */
    private static MultiCache<UUID, String, Object> contextCache = new MultiCache<>();

    /**
     * Sender of the command (can be null)
     */
    private T commandSender;

    /**
     * The instance of the command
     */
    @Setter
    private CommandInstance command;

    /**
     * The parameter of the command
     */
    @Setter
    private ParamSet paramSet;

    public CommandContext(T commandSender) {
        this.commandSender = commandSender;
    }

    /**
     * Gets the unique id of the sender, that means either the {@link #CONSOLE_UUID} or the uuid of the player
     *
     * @return The uuid
     */
    protected abstract UUID getSendersUniqueId();

    /**
     * Sends a message to given target
     *
     * @param target The target
     * @param msg    The message
     */
    protected abstract void message(String msg, T target);

    /**
     * Checks if the sender is the console
     *
     * @return The result
     */
    public boolean isConsole() {
        return getSendersUniqueId().equals(CONSOLE_UUID);
    }

    /**
     * Sends a message to all targets or to the commandSender
     *
     * @param msg     The msg
     * @param targets The targets
     */
    public void sendMessage(String msg, T... targets) {
        if(targets == null || targets.length == 0) {
            message(msg, commandSender);
        }
        else {
            for(T target : targets) {
                message(msg, target);
            }
        }
    }

    public void sendMessage(List<String> messages, T... targets) {
        messages.forEach(s -> sendMessage(s, targets));
    }

    /**
     * Sends a message only to the command sender
     *
     * @param msg The message as string
     */
    public void sendMessages(String... msg) {
        sendMessage(Arrays.asList(msg));
    }

    /**
     * Throws an invalid usage exception and therefore triggers the argument helper
     *
     * @see InvalidCommandUsageException
     * @see ArgumentHelper
     */
    public void invalidUsage(Object... params) {
        EventExecutor.getInstance().execute(new CommandErrorEvent<>(this, command,
                new InvalidCommandUsageException(InvalidCommandUsageException.Type.CUSTOM_EVENTABLE, command, params)));
    }

    /**
     * Throws an invalid usage exception to stop the command and to send a message
     *
     * @param message The message
     * @param help    If the command helper should be triggered too
     * @throws InvalidArgumentException To cancel the command execution
     */
    public void invalidArgument(String message, boolean help) throws InvalidArgumentException {
        throw new InvalidArgumentException(InvalidArgumentException.Type.CUSTOM, message).commandHelp(help);
    }

    public void invalidArgument(String message) throws InvalidArgumentException {
        invalidArgument(message, false);
    }

    /**
     * Similar to {@link #invalidArgument(String, boolean)} but with a condition before throwing
     * an invalid argument exception
     *
     * @param condition The condition which should be true
     * @param help      Should the argument helper be called too?
     * @param message   The message to be sent after the condition is true
     * @throws InvalidArgumentException If the condition is true (To cancel the command execution)
     */
    public void invalidArgument(boolean condition, boolean help, String message) throws InvalidArgumentException {
        if(condition) invalidArgument(message, help);
    }

    public void invalidArgument(boolean condition, String msg, Object... replacements) throws InvalidArgumentException {
        String finalMessage = LanguageManager.contains(msg) ? LanguageManager.get(msg, replacements) : msg;
        invalidArgument(condition, false, finalMessage);
    }

    public void invalidArgument(Object value, String msg, Object... replacements) {
        invalidArgument(value == null, msg, replacements);
    }

    /**
     * Executes the argument helper with current argument and given params
     *
     * @param params The parameter
     */
    public boolean sendHelp(Object... params) {
        return sendHelpCurrent(false, params);
    }

    public boolean sendHelpCurrent(boolean before, Object... params) {
        ArgumentHelper<T> helper = new ArgumentHelper<>(this, getParamSet().size() + (before ? -1 : 0), params);
        command.executeArgumentHelper(helper);
        boolean r = false;

        for(String s : helper.getElement()) {
            sendMessage(s);

            if(!r) r = true;
        }
        return r;
    }

    /**
     * Sends the usage to the commandSender
     *
     * @param prefix The prefix (e.g. a color or whatever)
     */
    public void sendUsage(String prefix, boolean highlighted) {
        sendMessage(prefix + getUsage(highlighted, false));
    }

    public void sendUsage(String prefix) {
        sendUsage(prefix, false);
    }

    public void sendUsage() {
        sendUsage("");
    }

    /**
     * Similar to {@link #sendUsage(String, boolean)}<br>
     * Current means the param he recently entered
     *
     * @param prefix      The prefix
     * @param highlighted Highlighted the current param
     */
    public void sendCurrentUsage(String prefix, boolean highlighted) {
        sendMessage(prefix + getUsage(highlighted, true));
    }

    public void sendCurrentUsage(String prefix) {
        sendCurrentUsage(prefix, true);
    }

    public void sendCurrentUsage() {
        sendCurrentUsage("");
    }

    /**
     * Gets the current argument of the usage with using the parameter size
     *
     * @param before Before means either ('1 2 3'=3 or '1 2 3'=2)
     * @return The key
     */
    public String getCurrentUsageArg(boolean before) {
        int size = getParamSet().size();
        String param = command.getUsage().getParam(size - (before ? 1 : 0));
        if(param == null) return "";
        return param;
    }

    /**
     * Gets the usage of the inherited command
     *
     * @return The usage as string
     */
    public String getUsage(boolean highlighted, boolean current) {
        if(command == null) return "";

        String usage = "/";
        usage = (highlighted ? "§7" : "") + usage + command.getPath().replace(".", " ");
        usage += " " + command.getUsage().getBase();

        // list current arg
        String currentArg = getCurrentUsageArg(current);
        usage = usage.replace(currentArg, "§c" + currentArg + "§7");

        return usage;
    }

    public String getUsage() {
        return getUsage(false, false);
    }

    /**
     * Throws an invalid usage exception when the args size of {@link ParamSet} is not equals given value
     *
     * @param args The arguments
     * @param size The needed size
     * @see #invalidUsage(Object...)
     */
    public void checkParameterSize(ParamSet args, int size) {
        if(args.size() == size) return;
        this.invalidUsage();
    }

    /**
     * Get the message format sender which can be used to easily send non-pageable lists
     *
     * @param format The format (text to-be-formatted)
     * @return The instance of the sender
     */
    public MessageFormatSender getFormatSender(String format) {
        return new MessageFormatSender(format) {
            @Override
            public void sendMessage(String s) {
                CommandContext.this.sendMessage(s);
            }
        };
    }

    /**
     * Stores a value with the key temporarily
     *
     * @param key              The key
     * @param value            The value
     * @param policy           The policy of the expiration
     * @param duration         The duration
     * @param unit             The unit
     * @param removalListeners The listeners if the value is removed
     */
    public void set(String key, Object value, ExpiringMap.ExpirationPolicy policy, long duration, TimeUnit unit, Consumer... removalListeners) {
        UUID uuid = getSendersUniqueId();
        String realKey = getCommand().getLabel() + ":" + key;

        contextCache.put(uuid, realKey, value, policy, duration, unit, removalListeners);
    }

    public void set(String key, Object value, Consumer... removalListeners) {
        this.set(key, value, null, -1, null, removalListeners);
    }

    /**
     * Similar function as {@link #set(String, Object, ExpiringMap.ExpirationPolicy, long, TimeUnit, Consumer[])}
     * but the value stored inside will be refreshed after every access that means it's expiration time
     * will be set to {@code duration} again
     *
     * @param key              The key
     * @param value            The value
     * @param duration         The duration
     * @param unit             The time unit
     * @param removalListeners The listeners if the value is removed
     */
    public void setExpireAfterAccess(String key, Object value, long duration, TimeUnit unit, Consumer... removalListeners) {
        set(key, value, ExpiringMap.ExpirationPolicy.ACCESSED, duration, unit, removalListeners);
    }

    /**
     * Similar function as {@link #set(String, Object, ExpiringMap.ExpirationPolicy, long, TimeUnit, Consumer[])}
     * but the value stored inside will be removed after {@code duration} after setting the value
     *
     * @param key              The key
     * @param value            The value
     * @param duration         The duration
     * @param unit             The time unit
     * @param removalListeners The listeners if the value is removed
     */
    public void setExpireAfterCreation(String key, Object value, long duration, TimeUnit unit, Consumer... removalListeners) {
        set(key, value, ExpiringMap.ExpirationPolicy.CREATED, duration, unit, removalListeners);
    }

    /**
     * Gets a before stored value by key
     *
     * @param key The key
     * @return The value
     */
    public <V> V get(String key) {
        return (V) contextCache.get(getSendersUniqueId(),
                getCommand().getLabel() + ":" + key);
    }

}
