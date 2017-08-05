package de.superioz.moo.api.logging;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.events.MooLoggingEvent;
import jline.console.ConsoleReader;
import lombok.Getter;
import lombok.Setter;
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.logging.*;

/**
 * This logger is an extension of the native {@link Logger}<br>
 * It supports ANSI-colored text messages and it automatically logs everything
 * into a logging file. The native system streams are overriden to really logging EVERYTHING.<br>
 * To be able to print colored texts {@link AnsiConsole#systemInstall()} has to be called at the beginning of the program.<br>
 * At the end of the program use {@link AnsiConsole#systemUninstall()}
 * <p>
 * The single logging tasks are forwarded to a dispatcher which queues them in a {@link java.util.concurrent.LinkedBlockingQueue}
 * <p>
 * To close all handlers (to delete the .lck files) you have to use {@link MooLogger#close()}
 */
public class MooLogger extends Logger {

    @Getter
    private ConsoleReader reader;
    private final LogDispatcher dispatcher = new LogDispatcher(this);
    private ColoredWriter consoleHandler;

    @Setter @Getter
    private boolean debugMode = false;

    public MooLogger(String name, Formatter formatter) {
        super(name, null);
        super.setLevel(Level.ALL);

        // get jline console
        try {
            reader = new ConsoleReader();
            reader.setExpandEvents(false);
        }
        catch(IOException e) {
            System.err.println("Could not initialise console reader!");
            e.printStackTrace();
        }

        // checks the folder of logs
        try {
            this.consoleHandler = new ColoredWriter(reader);
            consoleHandler.setLevel(Level.ALL);
            consoleHandler.setFormatter(formatter);
            addHandler(consoleHandler);
        }
        catch(Exception ex) {
            System.err.println("Could not register logger!");
            ex.printStackTrace();
        }

        dispatcher.start();
    }

    public MooLogger(String name) {
        this(name, Loogger.DEFAULT_FORMATTING);
    }

    /**
     * Closes every handler
     */
    public void close() {
        for(Handler handler : getHandlers()) {
            handler.close();
        }
        dispatcher.interrupt();
    }

    @Override
    public void log(LogRecord record) {
        dispatcher.queue(record);
    }

    void doLog(LogRecord record) {
        super.log(record);

        // event for processing the record further (if needed)
        EventExecutor.getInstance().execute(new MooLoggingEvent(consoleHandler.getFormatter().format(record)));
    }

}
