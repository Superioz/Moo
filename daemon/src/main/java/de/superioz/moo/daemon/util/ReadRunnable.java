package de.superioz.moo.daemon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Consumer;

@Getter
@AllArgsConstructor
public class ReadRunnable implements Runnable {

    private BufferedReader reader;
    private Consumer<String> onRead;

    @Override
    public void run() {
        String line;
        try {
            while((line = reader.readLine()) != null){
                // do something with the line
                this.onRead.accept(line);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}
