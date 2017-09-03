package de.superioz.moo.api.modules;

import de.superioz.moo.api.event.EventExecutor;
import de.superioz.moo.api.events.RedisConnectionEvent;
import de.superioz.moo.api.module.Module;
import de.superioz.moo.api.redis.MooCache;
import de.superioz.moo.api.redis.RedisConnection;
import lombok.Getter;
import org.redisson.config.Config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This module is for connecting to Redis
 *
 * @see org.redisson.Redisson
 * @see de.superioz.moo.api.module.ModuleRegistry#register(Module...)
 */
@Getter
public class RedisModule extends Module {

    private File configFile;
    private Config config;
    private Logger logger;

    private RedisConnection redisConnection;

    public RedisModule(File configFile, Logger logger) {
        this.logger = logger;
        this.configFile = configFile;
        this.redisConnection = new RedisConnection();

        try {
            this.config = configFile.getName().endsWith(".json") ? Config.fromJSON(configFile) : Config.fromYAML(configFile);
        }
        catch(IOException e) {
            logger.severe("Error while loading Redis config! " + e);
            super.finished(false);
        }
    }

    @Override
    public String getName() {
        return "redis";
    }

    @Override
    protected void onEnable() {
        if(config == null) {
            logger.info("Can't connect to Redis because the config is null!");
            super.finished(false);
            return;
        }
        redisConnection.connectRedis(config);
        logger.info("Redis connection status: " + (redisConnection.isRedisConnected() ? "ON" : "off"));

        // loading the RedisCache
        MooCache.getInstance().initialize(redisConnection);

        // call event that redis changed its connection state
        EventExecutor.getInstance().execute(new RedisConnectionEvent(this, redisConnection.isRedisConnected()));
    }

    @Override
    protected void onDisable() {
        redisConnection.getClient().shutdown();

        // call event that redis got disconnected
        EventExecutor.getInstance().execute(new RedisConnectionEvent(this, false));
    }
}
