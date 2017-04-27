package de.superioz.moo.daemon.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class Ports {

    public static final int MIN_PORT = 40000;
    public static final int MAX_PORT = 50000;

    /**
     * Gets an available port
     *
     * @return The port
     */
    public static int getAvailablePort() {
        int port = MIN_PORT;

        for(int i = MIN_PORT; i < MAX_PORT; i++) {
            if(isAvailable(i)) {
                port = i;
                break;
            }
        }
        return port;
    }

    /**
     * Checks if a specific port is available
     *
     * @param port The port
     * @return The result
     */
    public static boolean isAvailable(int port) {
        if(port < MIN_PORT || port > MAX_PORT) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }

        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        }
        catch(IOException e) {
            //
        }
        finally {
            if(ds != null) {
                ds.close();
            }

            if(ss != null) {
                try {
                    ss.close();
                }
                catch(IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

}
