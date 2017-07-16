package de.superioz.moo.daemon.task;

import de.superioz.moo.daemon.Daemon;
import lombok.Getter;
import de.superioz.moo.client.Moo;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ServerStartQueueTask implements Runnable {

    private List<ServerStartTask> queue = new ArrayList<>();

    @Override
    public void run() {
        while(true){
            if(Moo.getInstance().isConnected()) {
                for(ServerStartTask task : queue) {
                    Daemon.server.getExecutors().execute(task);

                    try {
                        Thread.sleep(20);
                    }
                    catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                queue.clear();
            }

            try {
                Thread.sleep(1000);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
