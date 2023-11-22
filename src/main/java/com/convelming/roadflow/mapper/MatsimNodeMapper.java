package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.MatsimNode;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class MatsimNodeMapper {

    private final String TABLE_NAME = " matsim_node ";

    //    private final String INSERT_SQL = "insert into " + TABLE_NAME + "(srid, x, y, geom) values (?, ?, ?, ?)";
    private final String INSERT_SQL = "insert into " + TABLE_NAME + "(id, srid, x, y, geom) values (?, ?, ?, ?, ?)";

    @Resource
    JdbcTemplate jdbcTemplate;

    public boolean insert(MatsimNode node) {
        Object[] args = {
                node.getId(),
                node.getSrid(),
                node.getX(),
                node.getY(),
                node.getGeom()
        };
        int row = jdbcTemplate.update(INSERT_SQL, args);
        return row > 0;
    }

    public boolean deleteById(Object id) {
        int row = jdbcTemplate.update("delete from " + TABLE_NAME + " where id = ?", id);
        return row > 0;
    }

    public MatsimNode selectById(Object id) {
        return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where id = ?", new BeanPropertyRowMapper<>(MatsimNode.class), id);
    }

    public List<MatsimNode> selectByIds(Collection<Long> ids) {

        if(ids.isEmpty()){
            return new ArrayList<>();
        }

        StringBuilder sql = new StringBuilder("select * from ").append(TABLE_NAME).append(" where id in ( ");
        for (int i = 0, len = ids.size(); i < len; i++) {
            if (i != len - 1) {
                sql.append("? ,");
            } else {
                sql.append("?");
            }
        }
        sql.append(" )");
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(MatsimNode.class), ids.toArray());
    }

    public boolean batchInsert(List<MatsimNode> nodes) {
        List<Object[]> args = new ArrayList<>();
        for (MatsimNode node : nodes) {
            Object[] param = new Object[]{
                    node.getId(),
                    node.getSrid(),
                    node.getX(),
                    node.getY(),
                    node.getGeom()
            };
            args.add(param);
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, args);
        return true;
    }


}
