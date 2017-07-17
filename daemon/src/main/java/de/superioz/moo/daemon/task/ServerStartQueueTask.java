package de.superioz.moo.daemon.task;

import de.superioz.moo.client.Moo;
import de.superioz.moo.daemon.Daemon;
import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Getter
public class ServerStartQueueTask implements Runnable {

    private Queue<ServerStartTask> queue = new LinkedBlockingQueue<>();
    private ServerStartTask task;

    @Override
    public void run() {
        while(true){
            // if Moo is connected and one task is inside the queue
            if(Moo.getInstance().isConnected()
                    && (task = queue.poll()) != null) {
                Daemon.getInstance().getServer().getExecutors().execute(task);

                try {
                    Thread.sleep(20);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
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
