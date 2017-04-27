package de.superioz.moo.api.console;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.command.Command;
import de.superioz.moo.api.command.context.ConsoleCommandContext;
import de.superioz.moo.api.command.param.ParamSet;
import jline.console.ConsoleReader;
import lombok.Getter;
import de.superioz.moo.api.command.CommandInstance;
import de.superioz.moo.api.command.CommandRegistry;
import de.superioz.moo.api.command.context.CommandContext;
import de.superioz.moo.api.event.EventListener;
import de.superioz.moo.api.logging.ConsoleColor;
import de.superioz.moo.api.logging.Logs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandTerminal implements EventListener {

    /**
     * The executor service to run something async
     */
    private ExecutorService executorService
            = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("commanding-pool-%d").build());

    /**
     * Is the command terminal already initialised?
     */
    @Getter
    private boolean initialised = false;

    /**
     * The logger
     */
    @Getter
    private Logs logs;

    /**
     * Starts the commanding system of the console<br>
     * The commanding system is an implementation of the jline console
     * and it supports a prompt and some other nice features
     *
     * @param commandable Should commands directly be passed over
     * @param reader      The console reader for the jline implementation
     */
    public void start(boolean commandable, Logs logs, ConsoleReader reader) {
        if(initialised) return;
        this.logs = logs;

        CommandRegistry.getInstance().registerCommands(this);
        CommandRegistry.getInstance().registerEventAdapter(new ConsoleCommandEventAdapter());

        // task for executing commands (jline)
        executorService.execute(new TerminalTask(reader, s -> {
            if(!commandable) {
                return;
            }
            String[] args = s.split(" ");
            String command = args.length > 0 ? args[0] : s;


            if(!CommandRegistry.getInstance().hasCommand(command)) {
                CommandInstance similar = CommandRegistry.getInstance().getSimilarCommand(command);

                logs.info(ConsoleColor.RED + (similar == null ?
                        "This command is not known by the system! Use 'help' for help." :
                        "Wrong command! Did you mean '" + similar.getLabel() + "'?"));
            }
            else {
                if(!CommandRegistry.getInstance().executeCommand(args,
                        new ConsoleCommandContext(logs.getLogger()))) {
                    //logs.debug(ConsoleColor.RED + "Couldn't execute command '" + command + "'");
                }
            }
        }));

        initialised = true;
    }

    /**
     * Stops the executor service and therefore the commanding thread
     */
    public void stop() {
        executorService.shutdownNow();
        initialised = false;
    }

    @Command(label = "help")
    public void onHelp(CommandContext context, ParamSet args) {
        context.sendMessage("Available commands: {" + String.join(", ", CommandRegistry.getInstance().getCommands()) + "}");
    }

}
