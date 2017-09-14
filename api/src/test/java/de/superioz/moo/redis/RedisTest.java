package de.superioz.moo.redis;

import de.superioz.moo.api.keyvalue.FinalValue;
import net.jodah.concurrentunit.Waiter;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RList;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
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

        Waiter waiter = new Waiter();
        map.put("key", 20);
        new Thread(() -> {
            mapsLocalCache2(waiter);
            waiter.resume();
        }).start();


        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("SET");
        map.put("key", 25);
        try {
            waiter.await();
        }
        catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    void mapsLocalCache2(Waiter waiter) {
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);

        LocalCachedMapOptions options = LocalCachedMapOptions.defaults().maxIdle(10 * 1000).timeToLive(10 * 1000);
        RLocalCachedMap<String, Integer> map = client.getLocalCachedMap("myMap", options);

        System.out.println("KEY BEFORE: " + map.get("key"));

        try {
            Thread.sleep(2000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("KEY AFTER: " + map.get("key"));
        waiter.assertEquals(map.get("key"), 25);
    }

    @Test
    void mapsItemChange() {
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);

        LocalCachedMapOptions options = LocalCachedMapOptions.defaults().maxIdle(5 * 1000).timeToLive(5 * 1000);
        RLocalCachedMap<String, FinalValue<Integer>> map = client.getLocalCachedMap("myMap", options);

        FinalValue value = new FinalValue(15);
        map.put("test", value);

        Waiter waiter = new Waiter();

        System.out.println("Value: " + value.get());
        new Thread(() -> {
            mapsItemChange2(waiter);
            waiter.resume();
        }).start();

        try {
            Thread.sleep(1000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        value.set(18);
        map.put("test", value);
        System.out.println("VALUE SET.");

        try {
            waiter.await();
        }
        catch(Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    void mapsItemChange2(Waiter waiter){
        Config config = new Config();
        config.useSingleServer().setAddress("http://127.0.0.1:6379");
        RedissonClient client = Redisson.create(config);

        LocalCachedMapOptions options = LocalCachedMapOptions.defaults().maxIdle(10 * 1000).timeToLive(10 * 1000);
        RLocalCachedMap<String, FinalValue<Integer>> map = client.getLocalCachedMap("myMap", options);
        FinalValue<Integer> value = map.get("test");
        System.out.println("Value Before: " + value.get());

        try {
            Thread.sleep(2000);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }

        value = map.get("test");
        System.out.println("Value After: " + value.get());
        waiter.assertEquals(value.get(), 18);
    }

}
