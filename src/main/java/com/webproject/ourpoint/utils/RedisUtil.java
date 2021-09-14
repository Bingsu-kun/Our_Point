package com.webproject.ourpoint.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String getData(String key) {
        ValueOperations<String,String> valueOperations = stringRedisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setData(String key, String value, Long expiryTime) {
        if (expiryTime != null) {
            stringRedisTemplate.opsForValue().set(key,value,expiryTime, TimeUnit.SECONDS);
        }
        else {
            stringRedisTemplate.opsForValue().set(key,value);
        }
    }

    public boolean isExists(String key) {
        return stringRedisTemplate.hasKey(key);
    }

    public void setExpiryTime(String key, Long expiryTime) {
        stringRedisTemplate.expire(key, expiryTime, TimeUnit.SECONDS);
    }

    public long getExpiryTime(String key) {
        return stringRedisTemplate.getExpire(key);
    }

    public void deleteData(String key) {
        stringRedisTemplate.delete(key);
    }
}
