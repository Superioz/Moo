package de.superioz.moo.redis;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisTest {

    @Test
    public void connectingToRedisWorks() {
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");

        RedissonClient client = Redisson.create(config);
        RList<String> list = client.getList("myList");
        System.out.println("List Before(" + list.size() + "): " + list);
        changeList(config);
        System.out.println("List After(" + list.size() + "): " + list);
    }

    private void changeList(Config config) {
        RedissonClient client2 = Redisson.create(config);
        client2.getList("myList").add("foo");
    }

    @Test
    public void mapsSpeed() {
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);
        
        RMap<String, Integer> map = client.getMap("myMap");

        long timeStamp1 = System.currentTimeMillis();
        System.out.println("Before Put");
        map.put("test", 15);
        System.out.println("After Put (" + (System.currentTimeMillis() - timeStamp1) + "ms)");

        long timeStamp2 = System.currentTimeMillis();
        System.out.println("Before Get");
        System.out.println("Get: " + map.get("test"));
        System.out.println("After Get (" + (System.currentTimeMillis() - timeStamp2) + "ms)");

        changeMap(config);
        System.out.println("After change: " + map.get("test"));
    }

    private void changeMap(Config config) {
        RedissonClient client2 = Redisson.create(config);
        client2.getMap("myMap").fastPut("test", 18);
    }


}
