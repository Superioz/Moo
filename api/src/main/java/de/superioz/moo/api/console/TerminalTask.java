package de.superioz.moo.api.console;

import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * The task to catch commands at console input
 */
public class TerminalTask implements Runnable {

    /**
     * Prompt of the jline console
     *
     * @see ConsoleReader
     * @see #reader
     */
    public static final String PROMPT = ">";

    /**
     * The console reader
     */
    private ConsoleReader reader;

    /**
     * The consumer when a new line is entered
     */
    @Getter
    private Consumer<String> newLine;

    public TerminalTask(ConsoleReader reader, Consumer<String> newLine) {
        this.reader = reader;
        this.newLine = newLine;
    }

    @Override
    public void run() {
        try {
            //TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, UnsupportedTerminal.class);
            Terminal terminal = TerminalFactory.get();
            terminal.setEchoEnabled(true);

            // set prompt and tab completion
            reader.setPrompt(PROMPT);
            reader.setCompletionHandler(new CandidateCompletionHandler());
            reader.addCompleter(new CandidateCompleter());

            String line;
            while((line = reader.readLine()) != null){
                newLine.accept(line);
            }
        }
        catch(Exception e) {
            // mostly this error happens when the console is not exited properly
            // I only witnesses it while executing the cloud on linux and closing
            // the console forcefully
            System.err.println("Error while JLine Terminal! (" + e.getMessage() + ")");
        }
        finally {
            try {
                TerminalFactory.get().restore();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
