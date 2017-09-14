package de.superioz.moo.api.logging;

import jline.console.ConsoleReader;
import org.fusesource.jansi.Ansi;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * This is an extension for a writer handler of java logging
 * It is able to write colors into the console
 */
public class ColoredWriter extends Handler {

    public static final Map<ConsoleColor, String> REPLACEMENT_BY_COLOR = new EnumMap<>(ConsoleColor.class);
    private final ConsoleColor[] colors = ConsoleColor.values();
    private final ConsoleReader console;

    /*
    Adds ansi code for console color inside the replacement map
     */ {
        REPLACEMENT_BY_COLOR.put(ConsoleColor.BLACK, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.DARK_BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.DARK_GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.DARK_AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.DARK_RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.DARK_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.GOLD, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.DARK_GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.LIGHT_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.YELLOW, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.WHITE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.MAGIC, Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.BOLD, Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.STRIKETHROUGH, Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.UNDERLINE, Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.ITALIC, Ansi.ansi().a(Ansi.Attribute.ITALIC).toString());
        REPLACEMENT_BY_COLOR.put(ConsoleColor.RESET, Ansi.ansi().a(Ansi.Attribute.RESET).toString());
    }

    public ColoredWriter(ConsoleReader console) {
        this.console = console;
    }

    /**
     * Prints a message to the console
     *
     * @param s The message (can contain colors)
     */
    public void print(String s) {
        // colorize the message
        s = ConsoleColor.replaceColors(s);

        try {
            console.print(Ansi.ansi().eraseLine(Ansi.Erase.ALL).toString() + ConsoleReader.RESET_LINE + s + Ansi.ansi().reset().toString());
            console.drawLine();
            console.flush();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void publish(LogRecord record) {
        if(isLoggable(record)) {
            print(getFormatter().format(record));
        }
    }

    @Override
    public void flush() {
        //
    }

    @Override
    public void close() throws SecurityException {
        //
    }

}
