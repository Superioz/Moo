package de.superioz.moo;

import de.superioz.moo.client.Moo;
import de.superioz.moo.protocol.client.ClientType;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.packets.PacketPing;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class CloudConnectionTest {

    @Test
    void benchmarkPacketResponse() {
        Moo.initialize(Logger.getLogger("test"));
        Moo.getInstance().connect("test", ClientType.CUSTOM, "127.0.0.1", 8000);
        Moo.getInstance().waitForAuthentication();

        Moo.getInstance().getExecutors().execute(() -> {
            System.out.println("Let's go!");
            int sent = 0;
            final int[] received = {0};

            long timestamp = System.currentTimeMillis();
            for(int i = 0; i < 100; i++) {
                sent++;
                CompletableFuture future = new CompletableFuture();
                PacketMessenger.transfer(new PacketPing(), (Consumer<PacketPing>) response -> {
                    received[0]++;
                    future.complete(null);
                });
                try {
                    future.get(1, TimeUnit.SECONDS);
                }
                catch(CancellationException | InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Get result .. " + (System.currentTimeMillis() - timestamp) + "ms");
            try {
                Thread.sleep(3000L);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("================================");
            System.out.println("Sent: " + sent + "/Received: " + received[0]);

            assert sent == received[0];
        });

        Moo.getInstance().waitForShutdown();
    }

    @AfterAll
    void disconnectCloud() {
        Moo.getInstance().disconnect();
    }


}
