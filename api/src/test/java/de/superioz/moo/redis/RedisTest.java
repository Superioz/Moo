package de.superioz.moo.redis;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;

public class RedisTest {

    @Test
    void connectingToRedisWorks() {
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");

        RedissonClient client = Redisson.create(config);
        RList<String> list = client.getList("myList");
        int sizeBefore = list.size();
        changeList(config);
        assert list.size() > sizeBefore;
    }

    void changeList(Config config) {
        RedissonClient client2 = Redisson.create(config);
        client2.getList("myList").add("foo");
    }

    @Test
    void mapsLocalCache() {
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);

        LocalCachedMapOptions options = LocalCachedMapOptions.defaults().maxIdle(5 * 1000).timeToLive(5 * 1000);
        RLocalCachedMap<String, Integer> map = client.getLocalCachedMap("myMap", options);

        map.put("key", 20);
        new Thread(this::mapsLocalCache2).start();

        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("SET");
        map.put("key", 25);
    }

    void mapsLocalCache2() {
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);

        LocalCachedMapOptions options = LocalCachedMapOptions.defaults().maxIdle(10 * 1000).timeToLive(10 * 1000);
        RLocalCachedMap<String, Integer> map = client.getLocalCachedMap("myMap", options);

        System.out.println("KEY: " + map.get("key"));

        try {
            Thread.sleep(2000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        assert map.get("key") == 25;
    }


}
