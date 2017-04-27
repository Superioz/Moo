package de.superioz.moo.daemon.common;

import lombok.Getter;
import de.superioz.moo.daemon.util.ReadRunnable;

import java.io.*;
import java.util.function.Consumer;

@Getter
public class Console {

    private Server server;
    private Server parent;
    private Process process;

    private ReadRunnable readRunnable;
    private ReadRunnable errorRunnable;

    private BufferedReader reader;
    private BufferedWriter writer;
    private BufferedReader error;

    public Console(Server server, Server parent) {
        this.server = server;
        this.parent = parent;
        this.process = parent.getProcess();
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        this.error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    }

    /**
     * Starts the console
     */
    public Console start(Consumer<String> infoRead, Consumer<String> errorRead) {
        this.readRunnable = new ReadRunnable(reader, infoRead);
        this.errorRunnable = new ReadRunnable(error, errorRead);

        server.getExecutors().execute(readRunnable);
        server.getExecutors().execute(errorRunnable);
        return this;
    }

    /**
     * Closes all streams
     */
    public void close() {
        try {
            this.reader.close();
            this.error.close();
            this.writer.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads a line from a buffered reader of this console
     *
     * @param reader The reader
     * @return The line
     */
    private String read(BufferedReader reader) {
        if(readRunnable == null) return "";
        try {
            return reader.readLine();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String readError() {
        return read(readRunnable.getReader());
    }

    public String readInfo() {
        return read(errorRunnable.getReader());
    }

    /**
     * Writes a line into the console
     *
     * @param line The line
     * @return The result
     */
    public boolean write(String line) {
        try {
            writer.write(line);
            writer.flush();
        }
        catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
