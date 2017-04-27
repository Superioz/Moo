package de.superioz.moo.api.console;

import de.superioz.moo.api.utils.SystemUtil;
import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;

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
    private Consumer<String> newLine;

    public TerminalTask(ConsoleReader reader, Consumer<String> newLine) {
        this.reader = reader;
        this.newLine = newLine;
    }

    @Override
    public void run() {
        try {
            //TerminalFactory.registerFlavor(TerminalFactory.Flavor.WINDOWS, UnsupportedTerminal.class);
            Terminal terminal = TerminalFactory.getFlavor(SystemUtil.isWindows()
                    ? TerminalFactory.Flavor.WINDOWS : TerminalFactory.Flavor.UNIX);
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
            e.printStackTrace();
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
