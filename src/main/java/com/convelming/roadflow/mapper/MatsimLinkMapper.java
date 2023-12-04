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

    private final String INSERT_SQL = " insert into " + TABLE_NAME + "(id, name, srid, from_node, to_node, lane, length, freespeed, capacity, origid, type, geom, center) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, st_lineinterpolatepoint(st_linemerge(?), 0.5)) ";

    @Resource
    JdbcTemplate jdbcTemplate;

    /**
     * 新增
     * @param link
     * @return
     */
    public boolean insert(MatsimLink link) {
        int row = jdbcTemplate.update(INSERT_SQL, genArgs(link));
        return row > 0;
    }

    /**
     * 根据id删除
     * @param id
     * @return
     */
    public boolean deleteById(Object id) {
        int row = jdbcTemplate.update("delete from " + TABLE_NAME + " where id = ?", id);
        return row > 0;
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public MatsimLink selectById(Object id) {
        return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where id = ?", new BeanPropertyRowMapper<>(MatsimLink.class), id);
    }

    /**
     * 批量新增
     * @param links
     * @return
     */
    public boolean batchInsert(List<MatsimLink> links) {
        List<Object[]> args = new ArrayList<>();
        for (MatsimLink link : links) {
            Object[] param = genArgs(link);
            args.add(param);
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, args);
        return true;
    }

    /**
     * 根据origid查询
     * @param origid
     * @return
     */
    public List<MatsimLink> queryByOrigid(Long origid) {
        String sql = "select ml.*, string_agg(distinct ls.type, ',') as \"statsType\" from " + TABLE_NAME + " ml left join link_stats ls on ml.id = ls.link_id where ml.origid = ? group by ml.id";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MatsimLink.class), origid);
    }

    /**
     * 根据id查询反向link
     * @param id
     * @return
     */
    public MatsimLink queryReverseLink(Long id) {
        String sql = "select a.* from " + TABLE_NAME + " a left join " + TABLE_NAME + " b on a.to_node = b.from_node and a.from_node = b.to_node where b.id = ?";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(MatsimLink.class), id);
    }

    /**
     * 修改link信息
     * @param link
     * @return
     */
    public int update(MatsimLink link){
        String sql = "update " + TABLE_NAME + " set ";

        sql += " name = ?, ";       // 道路名称
        sql += " lane = ?, ";       // 车道数
        sql += " type = ?, ";       // 道路类型
        sql += " freespeed = ?,";   // 自由流量

        sql = sql.substring(0, sql.length() - 1);

        sql += " where id = ?";

        int row = jdbcTemplate.update(sql, new Object[]{
                link.getName(),
                link.getLane(),
                link.getType(),
                link.getFreespeed(),
                link.getId()});

        return row;
    }

    public int updateInWay(MatsimLink link){
        String sql = " update " + TABLE_NAME + " set ";

        sql += " name = ?, ";       // 道路名称
        sql += " lane = ?, ";       // 车道数
        sql += " type = ?, ";       // 道路类型
        sql += " freespeed = ?,";   // 自由流量

        sql = sql.substring(0, sql.length() - 1);

        sql += " where origid = ?";

        // 修改 link
        int row = jdbcTemplate.update(sql, new Object[]{
                link.getName(),
                link.getLane(),
                link.getType(),
                link.getFreespeed(),
                link.getOrigid()});

        // 修改 way
        jdbcTemplate.update("update osm_way set name = ?, highway = ? where id = ?", new Object[]{
            link.getName(),
            link.getType(),
            link.getOrigid()
        });

        return row;
    }

    private Object[] genArgs(MatsimLink link) {
        return new Object[]{
                link.getId(),
                link.getName(),
                link.getSrid(),
                link.getFromNode(),
                link.getToNode(),
                link.getLane(),
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
