package de.superioz.moo.daemon.task;

import de.superioz.moo.api.utils.SystemUtil;
import de.superioz.moo.client.Moo;
import de.superioz.moo.protocol.common.PacketMessenger;
import de.superioz.moo.protocol.exception.MooOutputException;
import de.superioz.moo.protocol.packets.PacketRamUsage;

public class RamUsageTask implements Runnable {

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(2000);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

            if(!Moo.getInstance().isConnected()) continue;
            try {
                PacketMessenger.transfer(new PacketRamUsage(SystemUtil.getCurrentRamUsage()));
            }
            catch(MooOutputException e) {
                //
            }
        }
    }

}
