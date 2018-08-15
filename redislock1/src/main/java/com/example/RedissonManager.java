package com.example;


import org.redisson.Redisson;
import org.redisson.config.Config;

public class RedissonManager {
        private static Config config;
        private static Redisson redisson;
        static {
            config = new Config();
            config.useSingleServer().setAddress("127.0.0.1:6379");
            redisson = (Redisson)Redisson.create(config);
        }

        public static Redisson getRedisson() {
            return redisson;
        }
}
