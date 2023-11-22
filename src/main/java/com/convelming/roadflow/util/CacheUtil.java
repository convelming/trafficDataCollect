package com.convelming.roadflow.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Component
public class CacheUtil {

    private final Map<String, Object> cache = new HashMap<>();
    private final Map<String, Object> effective = new HashMap<>();

    /**
     * OSM全部道路信息
     */
    public final static String ALL_OSM_WAY_KEY = "ALL_OSM_WAY";

    /**
     * 一天
     */
    public final static Long DAY = 1000 * 60 * 60 * 24L;

    /**
     *
     * @param key 缓存键
     * @param value 缓存值
     * @param eff 有效期时长单位秒
     */
    public void put(String key, Object value, long eff){
        cache.put(key, value);
        effective.put(key, LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() + eff);
    }

    /**
     *
     * @param key 键
     */
    public synchronized Object get(String key){
        Long eff = (Long) effective.get(key);
        if(eff == null || LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() > eff){ // 缓存过期
            cache.remove(key);
            effective.remove(key);
        }
        return null;
    }

}
