package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.proxy.MatsimLinkProxy;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.proxy.sql.GroupKeys;
import jakarta.annotation.Resource;
import net.postgis.jdbc.PGgeometry;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MatsimLinkMapper {

    private final String TABLE_NAME = " matsim_link ";

    private final String INSERT_SQL = " insert into " + TABLE_NAME + "(id, name, srid, from_node, to_node, lane, length, freespeed, capacity, origid, type, geom, center) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, st_lineinterpolatepoint(st_linemerge(?), 0.5)) ";

    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    private EasyEntityQuery eeq;

    /**
     * 新增
     *
     * @param link
     * @return
     */
    public boolean insert(MatsimLink link) {
//        int row = jdbcTemplate.update(INSERT_SQL, genArgs(link));
        long row = eeq.insertable(link).executeRows();
        return row > 0;
    }

    /**
     * 根据id删除
     *
     * @param id
     * @return
     */
    public boolean deleteById(String id) {
        long row = eeq.deletable(MatsimLink.class).where(t -> t.id().eq(id)).executeRows();
        return row > 0;
    }

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    public MatsimLink selectById(String id) {
        return eeq.queryable(MatsimLink.class).where(t -> t.id().eq(id)).singleOrNull();
    }


    public List<MatsimLink> selectLikeId(String id) {
        List<MatsimLink> list = eeq.queryable(MatsimLink.class).where(t -> t.id().like(id)).limit(100).toList();
        return list;
    }

    /**
     * 批量新增
     *
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

    public List<MatsimLink> selectIntersects(PGgeometry ggeometry) {
        if (ggeometry == null) {
            return new ArrayList<>();
        }
        String sql = "select * from " + TABLE_NAME + " where ST_Intersects(?, geom)";
        return eeq.sqlQuery(sql, MatsimLink.class, List.of(ggeometry));
//        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MatsimLink.class), ggeometry);
    }

    /**
     * 根据origid查询
     *
     * @param origid
     * @return
     */
    public List<MatsimLink> queryByOrigid(String origid) {
        List<MatsimLink> list = eeq.queryable(MatsimLink.class).where(t -> t.origid().eq(origid)).toList(MatsimLink.class);
        List<Map<String, Object>> types = eeq.queryable(LinkStats.class)
                .where(t -> {
                    t.linkId().in(list.stream().map(MatsimLink::getId).toList());
                    t.type().ne("3");
                })
                .groupBy(t -> GroupKeys.TABLE1.of(t.linkId()))
                .select("link_id, string_agg(distinct type, ',') type").toMaps();
        Map<String, String> linkTypeMap = new HashMap<>();
        types.forEach(map -> linkTypeMap.put((String) map.get("link_id"), (String) map.get("type")));
        list.forEach(link -> link.setStatsType(linkTypeMap.get(link.getId())));
        return list;
    }

    /**
     * 根据id查询反向link
     *
     * @param id
     * @return
     */
    public MatsimLink queryReverseLink(String id) {
        MatsimLink link = eeq.queryable(MatsimLink.class).leftJoin(MatsimLink.class, (a, b) -> {
            a.fromNode().eq(b.toNode());
            a.toNode().eq(b.fromNode());
        }).where((a, b) -> b.id().eq(id)).select((a, b) -> {
            MatsimLinkProxy proxy = new MatsimLinkProxy();
            proxy.selectAll(a);
            return proxy;
        }).singleOrNull();
        return link;
    }

    /**
     * 修改link信息
     *
     * @param link
     * @return
     */
    public long update(MatsimLink link) {
        long row = eeq.updatable(MatsimLink.class).setColumns(t -> {
            t.name().set(link.getName());
            t.lane().set(link.getLane());
            t.type().set(link.getType());
            t.freespeed().set(link.getFreespeed());
        }).where(t -> t.id().eq(link.getId())).executeRows();
        return row;
    }

    public long updateInWay(MatsimLink link) {
        long row = eeq.updatable(MatsimLink.class).setColumns(t -> {
            t.name().set(link.getName());
            t.lane().set(link.getLane());
            t.type().set(link.getType());
            t.freespeed().set(link.getFreespeed());
        }).where(t -> t.origid().eq(link.getOrigid())).executeRows();


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
