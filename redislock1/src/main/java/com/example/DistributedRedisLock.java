package com.example;

import org.redisson.Redisson;
import org.redisson.api.RLock;

import java.util.concurrent.TimeUnit;

public class DistributedRedisLock {
    private static Redisson redisson;

    static {
        redisson = RedissonManager.getRedisson();
    }

    public static boolean lock(String key) {
        RLock mylock = redisson.getLock(key);
        mylock.lock(10, TimeUnit.MINUTES);
        return Thread.currentThread().isInterrupted() == false;
    }

    public static void unlock(String key) {
        RLock mylock = redisson.getLock(key);
        mylock.unlock();
    }

}
