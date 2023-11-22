package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.MatsimLink;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MatsimLinkMapper {

    private final String TABLE_NAME = " matsim_link ";

    private final String INSERT_SQL = " insert into " + TABLE_NAME + "(id, srid, from_node, to_node, length, freespeed, capacity, origid, type, geom, center) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, st_lineinterpolatepoint(st_linemerge(?), 0.5)) ";

    @Resource
    JdbcTemplate jdbcTemplate;

    public boolean insert(MatsimLink link) {
        int row = jdbcTemplate.update(INSERT_SQL, genArgs(link));
        return row > 0;
    }

    public boolean deleteById(Object id) {
        int row = jdbcTemplate.update("delete from " + TABLE_NAME + " where id = ?", id);
        return row > 0;
    }

    public MatsimLink selectById(Object id) {
        return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where id = ?", new BeanPropertyRowMapper<>(MatsimLink.class), id);
    }

    public boolean batchInsert(List<MatsimLink> links) {
        List<Object[]> args = new ArrayList<>();
        for (MatsimLink link : links) {
            Object[] param = genArgs(link);
            args.add(param);
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, args);
        return true;
    }

    public List<MatsimLink> queryByOrigid(Long origid){
        String sql = " select ml.*, string_agg(distinct ls.type, ',') as \"statsType\" from " + TABLE_NAME + " ml left join link_stats ls on ml.id = ls.link_id where origid = ? group by ml.id ";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MatsimLink.class), origid);
    }

    private Object[] genArgs(MatsimLink link) {
        return new Object[]{
                link.getId(),
                link.getSrid(),
                link.getFromNode(),
                link.getToNode(),
                link.getLength(),
                link.getFreespeed(),
                link.getCapacity(),
                link.getOrigid(),
                link.getType(),
                link.getGeom(),
                link.getGeom()
        };
    }
}
