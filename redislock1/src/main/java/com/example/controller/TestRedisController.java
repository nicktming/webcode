package com.example.controller;

import com.example.DistributedRedisLock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/redis")
public class TestRedisController {

    static RedisTemplate<String, Integer> redisTemplate = new RedisTemplate();
    static int countWithoutLock = 0;
    static int countWithLock = 0;

    @GetMapping("/withlock")
    public Mono<Void> withLock() {
        String key = "test";
        boolean getLock = DistributedRedisLock.lock(key);
        try {
            System.out.println("countWithLock:" + countWithLock++);
        } finally {
            DistributedRedisLock.unlock(key);
        }
        return Mono.empty();
    }


    @GetMapping("/withoutlock")
    public Mono<Void> withoutLock() {
        System.out.println("countWithoutLock:" + countWithoutLock++);
        return Mono.empty();
    }

}
