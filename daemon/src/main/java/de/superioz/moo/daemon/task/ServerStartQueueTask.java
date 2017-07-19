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

    private static final int MAX_WAIT_COUNT = 10;

    @Override
    public void run() {
        while(true){
            // if Moo is connected and one task is inside the queue
            if(Moo.getInstance().isConnected()
                    && (task = queue.poll()) != null) {
                Daemon.getInstance().getServer().getExecutors().execute(task);
            }

            // if server null just wait
            if(task == null && queue.isEmpty()) {
                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // wait for other server to get finished
            int maxWaitTime = 0;
            while(task.getServer() == null || !task.getServer().isOnline()){
                if(maxWaitTime >= MAX_WAIT_COUNT) break;

                try {
                    Thread.sleep(1000);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
                maxWaitTime++;
            }
        }
    }
}
