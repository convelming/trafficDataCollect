package com.convelming.roadflow.util;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component // 默认单例
public class IdUtil {
    private final Map<String, Long> idMap = new ConcurrentHashMap<>();

    private static final String SELECT_SQL = " select max(id) from #{table} ";

    @Resource
    private JdbcTemplate jdbcTemplate;

    public Long getId(String table) {
        synchronized (IdUtil.class) {
            Long id = idMap.get(table);
            if (id == null) {
                String sql = SELECT_SQL.replace("#{table}", table);
                log.info("select id sql ==> : {}", sql);
                Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
                id = maxId == null ? 1 : maxId + 1;
            } else {
                id = id + 1;
            }
            idMap.put(table, id);
            return id;
        }
    }

    public void clear() {
        idMap.clear();
    }

}
