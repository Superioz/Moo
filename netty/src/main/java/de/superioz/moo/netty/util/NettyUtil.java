package de.superioz.moo.netty.util;

import de.superioz.moo.netty.exception.MooOutputException;

public class NettyUtil {

    /**
     * Checks if the current thread is an async moo pool task
     */
    public static void checkAsyncTask() {
        Thread currentThread = Thread.currentThread();
        if(currentThread.getName().equals("main")
                // I decided to not allow the nioEventLoopGroup as "async", because it could block
                // netty sending/receiving packets
                || currentThread.getName().startsWith("nioEventLoopGroup")) {
            throw new MooOutputException(MooOutputException.Type.WRONG_THREAD, currentThread.getName());
        }
    }

}
