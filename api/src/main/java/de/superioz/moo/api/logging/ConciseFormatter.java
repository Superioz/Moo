package de.superioz.moo.api.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This is just a simple formatter for a logger
 */
public class ConciseFormatter extends Formatter {

    private final DateFormat date = new SimpleDateFormat("HH:mm:ss.sss");
    private boolean stripColors = false;

    public ConciseFormatter(boolean stripColors) {
        this.stripColors = stripColors;
    }

    @Override
    @SuppressWarnings("ThrowableResultIgnored")
    public String format(LogRecord record) {
        StringBuilder formatted = new StringBuilder();

        formatted.append("[");
        formatted.append(date.format(record.getMillis()));
        formatted.append(" ");
        formatted.append(record.getLevel().getName());
        formatted.append("] ");
        formatted.append(stripColors ? ConsoleColor.stripColors(formatMessage(record)) : formatMessage(record));
        formatted.append('\n');

        if(record.getThrown() != null) {
            StringWriter writer = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(writer));
            formatted.append(writer);
        }
        return formatted.toString();
    }

}
