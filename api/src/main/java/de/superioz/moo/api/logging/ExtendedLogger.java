package de.superioz.moo.api.logging;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.superioz.moo.api.utils.LogUtil;
import de.superioz.moo.api.utils.SystemUtil;
import de.superioz.moo.api.utils.TimeUtil;
import lombok.Getter;
import lombok.Setter;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a wrapper class for a {@link Logger}<br>
 * You can use this class to simply wrap an existing logger (maybe with a file logging handler). For the file handler
 * you just have to use {@link #enableFileLogging()}<br>
 * <p>
 * If you are using this for a custom program I recommend to use the {@link MooLogger} because it supports colors and jline!
 */
public class ExtendedLogger {

    public static final Formatter DEFAULT_FORMATTING = new ConciseFormatter();
    private static final String LOG_FILE = "latest.log";
    private static final String LOG_FOLDER = "logs";
    private static final String COMPRESSED_LOG_FORMAT = "{0}-{1}.log.gz";

    private ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("logging-pool-%d").build());

    @Getter
    private final Logger baseLogger;
    @Getter @Setter
    private boolean debugMode = false;
    @Getter
    private boolean fileLogging = false;

    public ExtendedLogger(Logger baseLogger) {
        AnsiConsole.systemInstall();
        this.baseLogger = baseLogger;
    }

    /**
     * Disables the logger and shutdowns the ansi console
     */
    public void disable() {
        if(baseLogger instanceof MooLogger) {
            ((MooLogger) baseLogger).close();
        }
        AnsiConsole.systemUninstall();
        executor.shutdownNow();
    }

    /**
     * Hooks into the native system streams and overrides them
     * to also logging all that would be printed to {@link System#out} and {@link System#err}
     * <p>
     * This is useful if you want EVERYTHING from the original console into a logging file
     *
     * @return This
     */
    public ExtendedLogger prepareNativeStreams() {
        System.setErr(new PrintStream(new LoggingOutputStream(getBaseLogger(), Level.SEVERE), true));
        System.setOut(new PrintStream(new LoggingOutputStream(getBaseLogger(), Level.INFO), true));
        return this;
    }

    /**
     * Enables the file logging which will automatically logs everything from the console inside a file called latest.logging<br>
     * Before every handler creation {@link ExtendedLogger#clean(String)} will be called (to clean up the logs folder)
     *
     * @return This
     */
    public ExtendedLogger enableFileLogging() {
        if(fileLogging || baseLogger == null) return this;
        this.checkFolder();
        String path = LOG_FOLDER + "/" + LOG_FILE;

        try {
            this.clean(path);
            this.createLogFile();

            FileHandler fileHandler = new FileHandler(path, 1000 * 1024, 1, true);
            fileHandler.setFormatter(DEFAULT_FORMATTING);
            baseLogger.addHandler(fileHandler);
        }
        catch(Exception ex) {
            System.err.println("Could not register logger!");
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * Checks if the folder for the logging files exists
     * And if not then it will create the folder
     */
    private void checkFolder() {
        File file = new File(LOG_FOLDER);
        if(!file.exists()) {
            try {
                if(!file.mkdir()) {
                    System.err.println("Could not create logger's folder!");
                }
            }
            catch(Exception e) {
                System.err.println("Could not create logger's folder!");
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates the logging file
     * And also writes a header to the logging file to show some details when e.g. the file was created
     */
    private File createLogFile() {
        File file = new File(LOG_FOLDER + "/" + LOG_FILE);
        if(!file.exists()) {
            try {
                if(!file.createNewFile()) {
                    System.err.println("Could not create logging file!");
                }
            }
            catch(IOException e) {
                System.err.println("Could not create logging file!");
                e.printStackTrace();
            }
        }

        try {
            Files.write(file.toPath(), Arrays.asList(
                    "*** THIS IS A LOG FILE",
                    "*** START DATE: " + TimeUtil.getFormat(System.currentTimeMillis()),
                    "*** SYSTEM: " + SystemUtil.getCurrentUser() + " (" + SystemUtil.getVMName() + " " + SystemUtil.getVMVersion() + ")",
                    "*** JAVA: " + SystemUtil.getJavaVersion()),
                    Charset.forName("UTF-8"));
        }
        catch(IOException e) {
            //..
        }
        return file;
    }

    /**
     * Cleans the label where the logging files are located<br>
     * First step is to compress old logging files into a .gz archive (saves space)<br>
     * Second step is to delete old .lck and unnecessary files<br>
     * Last step is to move old archives into a folder which displays the month they were created in (e.g. 2017, January)
     *
     * @param path The label
     * @return The result
     */
    private boolean clean(String path) {
        boolean r = false;

        // compresses old latest.logging
        LogUtil.compressLastLog(path, LOG_FOLDER, COMPRESSED_LOG_FORMAT);

        // deletes other files
        File folder = new File(LOG_FOLDER);
        File[] files = folder.listFiles();
        if(files != null) {
            for(File file : files) {
                if(!file.getName().endsWith(".gz") && !file.getName().endsWith(".log")
                        && !file.isDirectory()) {
                    r = file.delete();
                }
            }
        }

        // puts compressed archives into a sub folder
        executor.execute(() -> {
            LogUtil.sortCompressedLogs(folder, LOG_FOLDER);
        });
        return r;
    }

    /**
     * Prepares the message
     */
    public void prepareMessage() {

    }

    /**
     * Prints a INFO message
     *
     * @param msg The message
     * @param thr Exception?
     */
    public void info(String msg, Throwable thr) {
        if(baseLogger != null) baseLogger.log(Level.INFO, msg, thr);
    }

    public void info(String msg) {
        info(msg, null);
    }

    /**
     * Prints a SEVERE message
     *
     * @param msg The message
     * @param thr Exception?
     */
    public void severe(String msg, Throwable thr) {
        if(baseLogger != null) baseLogger.log(Level.SEVERE, msg, thr);
    }

    public void severe(String msg) {
        severe(msg, null);
    }

    /**
     * Prints a WARNING message
     *
     * @param msg The message
     * @param thr Exception?
     */
    public void warning(String msg, Throwable thr) {
        if(baseLogger != null) baseLogger.log(Level.WARNING, msg, thr);
    }

    public void warning(String msg) {
        warning(msg, null);
    }

    /**
     * Prints a DEBUG message
     *
     * @param msg The message
     * @param thr Exception?
     */
    public void debug(String msg, Throwable thr) {
        if(baseLogger != null && isDebugMode()) {
            baseLogger.log(Level.FINE, msg, thr);
        }
    }

    public void debug(String msg) {
        debug(msg, null);
    }

    /**
     * Prints a debug message but as info level
     *
     * @param msg The message
     * @param thr Exception?
     */
    public void debugInfo(String msg, Throwable thr) {
        if(baseLogger != null && isDebugMode()) {
            baseLogger.log(Level.INFO, msg, thr);
        }
    }

    public void debugInfo(String msg) {
        debugInfo(msg, null);
    }

}
