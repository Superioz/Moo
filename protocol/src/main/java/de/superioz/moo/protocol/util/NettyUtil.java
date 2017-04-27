package de.superioz.moo.protocol.util;

import de.superioz.moo.protocol.exception.MooOutputException;

public class NettyUtil {

    /**
     * Checks if the current thread is an async moo pool task
     */
    public static void checkAsyncTask() {
        Thread currentThread = Thread.currentThread();
        if(currentThread.getName().equals("main")
                || currentThread.getName().startsWith("nioEventLoopGroup")) {
            throw new MooOutputException(MooOutputException.Type.WRONG_THREAD);
        }
    }

}
