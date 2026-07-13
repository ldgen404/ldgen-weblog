package com.ldgen.weblog.manager;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;
import java.util.function.Supplier;

@Component
@Slf4j
public class RedisCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public <T> T getOrLoad(String key, Duration ttl, Supplier<T> dbLoader) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                @SuppressWarnings("unchecked")
                T value = (T) cached;
                return value;
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级查询数据库, key: {}", key, e);
        }

        T loaded = dbLoader.get();
        if (loaded == null) {
            return null;
        }

        try {
            redisTemplate.opsForValue().set(key, loaded, ttl);
        } catch (Exception e) {
            log.warn("Redis 写入失败，已返回数据库结果, key: {}", key, e);
        }
        return loaded;
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis 删除缓存失败, key: {}", key, e);
        }
    }

    public void deleteByPrefix(String prefix) {
        try {
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys == null || keys.isEmpty()) {
                return;
            }
            redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("Redis 按前缀删除缓存失败, prefix: {}", prefix, e);
        }
    }

    public void deleteKeys(String... keys) {
        for (String key : keys) {
            delete(key);
        }
    }
}
