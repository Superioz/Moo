package de.superioz.moo.api.cache;

import lombok.Getter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * This class is for connecting to Redis, yeah ..
 */
@Getter
public final class MooRedis {

    private static MooRedis instance;

    public static MooRedis getInstance(){
        if(instance == null){
            instance = new MooRedis();
        }
        return instance;
    }

    private Config config;
    private RedissonClient client;

    /**
     * Connects to a redis server with given config.<br>
     * It is recommended to set the address and authentification to the config.<br>
     * Example: {@link org.redisson.config.SingleServerConfig#setAddress(URI)}
     *
     * @param config The redisson config
     */
    public void connectRedis(Config config) {
        if(this.client != null) return;
        this.config = config;

        if(config != null) {
            this.client = Redisson.create(config);
        }
    }

    /**
     * Connects to a redis server the same as {@link #connectRedis(Config)} does but
     * with automatically getting the {@link Config} from a file. The file has to be
     * either a .json or a .yml
     *
     * @param configFile The file of configuration
     * @throws IOException /
     * @see <a href="https://github.com/redisson/redisson/wiki/2.-Configuration">Configuration Wiki</a>
     */
    public void connectRedis(File configFile) throws IOException {
        this.connectRedis(configFile.getName().endsWith("json")
                ? Config.fromJSON(configFile) : Config.fromYAML(configFile));
    }

    /**
     * Check if this instance is connected to Redis
     *
     * @return The result
     */
    public boolean isRedisConnected() {
        return client != null && client.getNodesGroup().pingAll();
    }

}
