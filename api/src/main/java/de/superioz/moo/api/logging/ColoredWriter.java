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

    private final Map<ConsoleColor, String> replacementByColor = new EnumMap<>(ConsoleColor.class);
    private final ConsoleColor[] colors = ConsoleColor.values();
    private final ConsoleReader console;

    /*
    Adds ansi code for console color inside the replacement map
     */ {
        replacementByColor.put(ConsoleColor.BLACK, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString());
        replacementByColor.put(ConsoleColor.DARK_BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString());
        replacementByColor.put(ConsoleColor.DARK_GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString());
        replacementByColor.put(ConsoleColor.DARK_AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString());
        replacementByColor.put(ConsoleColor.DARK_RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString());
        replacementByColor.put(ConsoleColor.DARK_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString());
        replacementByColor.put(ConsoleColor.GOLD, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString());
        replacementByColor.put(ConsoleColor.GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString());
        replacementByColor.put(ConsoleColor.DARK_GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString());
        replacementByColor.put(ConsoleColor.BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString());
        replacementByColor.put(ConsoleColor.GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString());
        replacementByColor.put(ConsoleColor.AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString());
        replacementByColor.put(ConsoleColor.RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString());
        replacementByColor.put(ConsoleColor.LIGHT_PURPLE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString());
        replacementByColor.put(ConsoleColor.YELLOW, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString());
        replacementByColor.put(ConsoleColor.WHITE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString());
        replacementByColor.put(ConsoleColor.MAGIC, Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString());
        replacementByColor.put(ConsoleColor.BOLD, Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString());
        replacementByColor.put(ConsoleColor.STRIKETHROUGH, Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString());
        replacementByColor.put(ConsoleColor.UNDERLINE, Ansi.ansi().a(Ansi.Attribute.UNDERLINE).toString());
        replacementByColor.put(ConsoleColor.ITALIC, Ansi.ansi().a(Ansi.Attribute.ITALIC).toString());
        replacementByColor.put(ConsoleColor.RESET, Ansi.ansi().a(Ansi.Attribute.RESET).toString());
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
        for(ConsoleColor color : colors) {
            s = s.replaceAll("(?i)" + color.toString(), replacementByColor.get(color));
        }
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
