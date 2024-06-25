package com.convelming.roadflow.mapper;

import com.alibaba.fastjson.JSONArray;
import com.convelming.roadflow.model.OSMWay;
import com.convelming.roadflow.model.vo.OSMWayVo;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class OSMWayMapper {

    private static final String TABLE_NAME = " osm_way ";

    private static final String BASE_FIELD = " id, version, timestamp, uid, \"user\", changeset, nodes, oneway, highway, geom4326, geom3857, name, other ";

    private static final String INSERT_SQL = "insert into " + TABLE_NAME + " (" + BASE_FIELD + ") values (?, ?, ?, ?, ?, ?, to_json(?::json), ?, ?, ?, ?, ?, ?)";


    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private EasyEntityQuery eeq;

    public boolean insert(OSMWay way) {
        long row = eeq.insertable(way).executeRows();
        return row > 0;
    }

    public boolean deleteById(String id) {
        long row = eeq.deletable(OSMWay.class).where(t -> t.id().eq(id)).executeRows();
        return row > 0;
    }

    public OSMWay selectById(String id) {
        return eeq.queryable(OSMWay.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    public Long queryStartNode(String id) {
        OSMWay way = this.selectById(id);
        List<Long> nodes = JSONArray.parseArray(way.getNodes(), Long.class);
        return nodes.get(0);
    }

    public boolean batchInsert(List<OSMWay> ways) {
        List<Object[]> args = new ArrayList<>();
        for (OSMWay way : ways) {
            args.add(genArgs(way));
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, args);
        return true;
    }


    /**
     * 查询一个多边形内所有的路
     *
     * @param geometry 多边形. 为 null 查询全部
     * @return 路
     */
    public List<OSMWay> queryByPolygon(PGgeometry geometry) {
        String sql;
        if (geometry == null) {
            sql = " select * from " + TABLE_NAME + " where highway is not null";
            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OSMWay.class));
        } else {
            sql = " select * from " + TABLE_NAME + " where st_intersects(?, geom3857) and highway is not null and id in (select origid from matsim_link) ";
            return jdbcTemplate.queryForList(sql, OSMWay.class, geometry);
        }
    }

    /**
     * 查询所有的路
     *
     * @return geojson 数据
     */
    public List<OSMWayVo> queryAllGeojson() {
        return queryByPolygonGeojson(null);
    }

    /**
     * 查询一个多边形内所有的路
     *
     * @param geometry 多边形
     * @return geojson 数据
     */
    public List<OSMWayVo> queryByPolygonGeojson(PGgeometry geometry) {
        String sql =
                " select t.id, st_asgeojson(t.geom3857) geom from osm_way t ";
        if (geometry == null) {
            sql += " where t.id in (select origid from matsim_link) ";
        } else {
            sql += " where st_intersects(?, geom3857) " +
                    " and t.id in (select origid from matsim_link) ";
        }
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OSMWayVo.class));
//        log.info("sql ==> :{}", sql);
//        log.info("param ==> :{}", geometry);
    }

    public List<OSMWay> queryByName(String name) {
        String sql = " select * from " + TABLE_NAME + " where name like ? and highway is not null";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OSMWay.class), "%" + name + "%");
    }

    private Object[] genArgs(OSMWay way) {
        return new Object[]{
                way.getId(),
                way.getVersion(),
                way.getTimestamp(),
                way.getUid(),
                way.getUser(),
                way.getChageset(),
                way.getNodes(),
                way.getOneway(),
                way.getHighway(),
                way.getGeom4326(),
                way.getGeom3857(),
                way.getName(),
                way.getOther()
        };
    }

}
