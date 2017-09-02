package de.superioz.moo.daemon.task;

import de.superioz.moo.api.utils.SystemUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.network.common.PacketMessenger;
import de.superioz.moo.network.exception.MooOutputException;
import de.superioz.moo.network.packets.PacketRamUsage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RamUsageTask implements Runnable {

    private int delay;

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(delay);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

            if(!Moo.getInstance().isConnected()) continue;
            try {
                PacketMessenger.message(new PacketRamUsage(SystemUtil.getCurrentRamUsage()));
            }
            catch(MooOutputException e) {
                e.printStackTrace();
                //
            }
        }
    }

}
