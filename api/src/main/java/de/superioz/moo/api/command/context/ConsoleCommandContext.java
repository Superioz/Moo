package de.superioz.moo.api.command.context;

import de.superioz.moo.api.console.format.DisplayFormat;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Instance of a {@link CommandContext} but specifically for the console
 */
public class ConsoleCommandContext extends CommandContext {

    private Logger logger;

    public ConsoleCommandContext(Logger logger) {
        super(null);
        this.logger = logger;
    }

    @Override
    protected UUID getSendersUniqueId() {
        return CONSOLE_UUID;
    }

    @Override
    protected void message(String msg, Object target) {
        logger.info(msg);
    }

    @Override
    public void sendDisplayFormat(DisplayFormat format, Object[] receivers) {
        format.prepare();

        format.getComponents().forEach((s, bool) -> logger.info(s));
    }
}
