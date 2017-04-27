package de.superioz.moo.api.logging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.LogRecord;

/**
 * This class dispatches every logging record in another thread. The logging records are added to the queue
 * and then published.
 */
public class LogDispatcher extends Thread {

    private MooLogger logger;
    private final BlockingQueue<LogRecord> queue = new LinkedBlockingQueue<>();

    public LogDispatcher(MooLogger logger) {
        super("Moo logging thread");
        this.logger = logger;
    }

    @Override
    public void run() {
        while(!isInterrupted()){
            LogRecord record;
            try {
                record = queue.take();
            }
            catch(InterruptedException ex) {
                continue;
            }

            logger.doLog(record);
        }
        for(LogRecord record : queue) {
            logger.doLog(record);
        }
    }

    public void queue(LogRecord record) {
        if(!isInterrupted()) {
            queue.add(record);
        }
    }

}
