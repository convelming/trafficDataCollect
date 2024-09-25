package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.OSMWay;
import com.convelming.roadflow.model.vo.LinkStatsAvg;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.base.MapProxy;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.proxy.sql.GroupKeys;
import jakarta.annotation.Resource;
import net.postgis.jdbc.PGgeometry;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LinkStatsMapper {

    private static final String TABLE_NAME = " link_stats ";

    private static final String BASE_FIELD = " id, link_id, way_id, begin_time, end_time, \"type\", pcu_h, scar, struck, mcar, mtruck, lcar, ltruck, video, is_two_way, x, y, remark, ip_addr, version, deleted, create_time, update_time ";

    public static final String INSERT_SQL = " insert into " + TABLE_NAME + " ( " + BASE_FIELD + " ) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";

    private static final String LIMIT_SQL = " limit ? offset ? ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public boolean insert(LinkStats stats) {
        stats.setId(idUtil.getId(TABLE_NAME));
        stats.setVersion(1);
        stats.setDeleted(0L);
//        Object[] args = new Object[]{
//                stats.getId(),
//                stats.getLinkId(),
//                stats.getWayId(),
//                stats.getBeginTime(),
//                stats.getEndTime(),
//                stats.getType(),
//                stats.getPcuH(),
//                stats.getScar(),
//                stats.getStruck(),
//                stats.getMcar(),
//                stats.getMtruck(),
//                stats.getLcar(),
//                stats.getLtruck(),
//                stats.getVideo(),
//                stats.getIsTwoWay(),
//                stats.getX(),
//                stats.getY(),
//                stats.getRemark(),
//                stats.getIpAddr(),
//                1,
//                0,
//                new Date(),
//                new Date()
//        };
//        String sql = INSERT_SQL;
//        for(int i=0; i<args.length; i++){
//            sql = sql.replace("?", String.valueOf(args[i]));
//        }
//        System.out.println(sql);
//        int row = jdbcTemplate.update(INSERT_SQL, args);
        long row = eeq.insertable(stats).executeRows();
        return row > 0;
    }

    public boolean update(LinkStats stats) {
//        String sql = " update " + TABLE_NAME + " set ";
//
//        sql += " link_id = ?, ";
//        sql += " way_id = ?, ";
//        sql += " begin_time = ?, ";
//        sql += " end_time = ?, ";
//        sql += " \"type\" = ?, ";
//        sql += " pcu_h = ?, ";
//        sql += " scar = ?, ";
//        sql += " struck = ?, ";
//        sql += " mcar = ?, ";
//        sql += " mtruck = ?, ";
//        sql += " lcar = ?, ";
//        sql += " ltruck = ?, ";
//        sql += " remark = ?, ";
//        sql += " ip_addr = ?, ";
//        sql += " version = version + 1, ";
//        sql += " video = ?, ";
//        sql += " is_two_way = ?, ";
//        sql += " update_time = now() ";
//
//        sql += " where id = ? ";

//        int row = jdbcTemplate.update(sql, new Object[]{
//                stats.getLinkId(),
//                stats.getWayId(),
//                stats.getBeginTime(),
//                stats.getEndTime(),
//                stats.getType(),
//                stats.getPcuH(),
//                stats.getScar(),
//                stats.getStruck(),
//                stats.getMcar(),
//                stats.getMtruck(),
//                stats.getLcar(),
//                stats.getLtruck(),
//                stats.getRemark(),
//                stats.getIpAddr(),
//                stats.getVideo(),
//                stats.getIsTwoWay(),
//                stats.getId()
//        });

        LinkStats org = selectById(stats.getId());

        stats.setUpdateTime(new Date());
        stats.setVersion(org.getVersion() + 1);

        long row = eeq.updatable(stats).executeRows();
        return row > 0;
    }

    public LinkStats selectById(Long id) {
        return eeq.queryable(LinkStats.class).where(s -> s.id().eq(id)).singleOrNull();
//        return jdbcTemplate.queryForObject(" select * from " + TABLE_NAME + " where id = ? and deleted = 0", new BeanPropertyRowMapper<>(LinkStats.class), id);
    }

    public List<LinkStats> queryAllMaker(Date beginTime, Date endTime, String type) {
//        String sql = " select distinct link_id, x, y, max(pcu_h) \"pcu_h\", string_agg(distinct type, ',') as \"type\" from " + TABLE_NAME + " ls where ls.deleted = 0 ";
//        sql += " and type != '3' ";
//        List<Object> args = new ArrayList<>();
//        if (type != null && !"".equals(type)) {
//            sql += " and ls.type = ? ";
//            args.add(type);
//        }
//        if (beginTime != null) {
//            sql += " and ls.begin_time >= ? ";
//            args.add(beginTime);
//        }
//        if (endTime != null) {
//            sql += " and ls.end_time <= ? ";
//            args.add(endTime);
//        }
//        sql += " group by link_id, x, y ";

//        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LinkStats.class), args.toArray());
        String columns = "distinct link_id, x, y, max(pcu_h) as \"pcu_h\", string_agg(distinct type, ',') as \"type\"";
        List<LinkStats> list = eeq.queryable(LinkStats.class)
                .where(s -> {
                    s.type().in(List.of("0", "1", "2")); // 除去3高德爬取
                    s.type().eq(type != null && !type.isEmpty(), type);
                    s.beginTime().ge(beginTime != null, beginTime);
                    s.endTime().le(endTime != null, endTime);
                })
                .groupBy(s -> GroupKeys.TABLE1.of(s.linkId(), s.x(), s.y()))
                .select(columns).toList(LinkStats.class);
        return list;
    }

    public List<LinkStats> queryByIds(Collection<Long> ids) {
        List<Map<String, Object>> maps = eeq.queryable(LinkStats.class)
                .leftJoin(MatsimLink.class, (a, b) -> a.linkId().eq(b.id()))
                .leftJoin(OSMWay.class, (a, b, c) -> b.origid().eq(c.id()))
                .where(a -> a.id().in(ids))
                .select((a, b, c) -> {
                    MapProxy result = new MapProxy();
                    result.put("linkLineString", b.geom());
                    result.put("wayLineString", c.geom3857());
                    result.selectAll(a);
                    return result;
                })
                .toMaps();
        List<LinkStats> list = new ArrayList<>();
        maps.forEach(map -> list.add(new LinkStats(map)));
        return list;
    }

    public List<LinkStatsAvg> queryAvgStats(Long[] ids, String linkId, String type) {
        String sql = " select " +
                "    to_char(begin_time, 'HH24') as \"hour\", " +
                "    avg(pcu_h) as \"pcu_h\", " +
                "    avg(scar / (extract(epoch from end_time - begin_time) / 3600)) as \"scar\", " +
                "    avg(mcar / (extract(epoch from end_time - begin_time) / 3600)) as \"mcar\", " +
                "    avg(lcar / (extract(epoch from end_time - begin_time) / 3600)) as \"lcar\", " +
                "    avg(struck / (extract(epoch from end_time - begin_time) / 3600)) as \"struck\", " +
                "    avg(mtruck / (extract(epoch from end_time - begin_time) / 3600)) as \"mtruck\", " +
                "    avg(ltruck / (extract(epoch from end_time - begin_time) / 3600)) as \"ltruck\" " +
                " from link_stats where deleted = 0 ";
//        String sql = " select " +
//                "    to_char(begin_time, 'HH24') as \"hour\", " +
//                "    avg(pcu_h) as \"pcu_h\", " +
//                "    case when avg(scar) = 0 then 0 else avg(scar / (extract(epoch from end_time - begin_time) / 3600)) end  as \"scar\", " +
//                "    case when avg(mcar) = 0 then 0 else avg(mcar / (extract(epoch from end_time - begin_time) / 3600)) end as \"mcar\", " +
//                "    case when avg(lcar) = 0 then 0 else avg(lcar / (extract(epoch from end_time - begin_time) / 3600)) end as \"lcar\", " +
//                "    case when avg(struck) = 0 then 0 else avg(struck / (extract(epoch from end_time - begin_time) / 3600)) end as \"struck\", " +
//                "    case when avg(mtruck) = 0 then 0 else avg(mtruck / (extract(epoch from end_time - begin_time) / 3600)) end as \"mtruck\", " +
//                "    case when avg(ltruck) = 0 then 0 else avg(ltruck / (extract(epoch from end_time - begin_time) / 3600)) end as \"ltruck\" " +
//                " from link_stats where deleted = 0 ";
        List<Object> pram = new ArrayList<>();
        if (type != null && !type.isEmpty()) {
            sql += "and type = ? ";
            pram.add(type);
        }
        if (ids == null || ids.length == 0) {
            sql += " and link_id = ? group by to_char(begin_time, 'HH24') ";
//            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LinkStatsAvg.class), linkId);
            pram.add(linkId);
            return eeq.sqlQuery(sql, LinkStatsAvg.class, pram);
        } else {
            sql += " and id in (";
            for (Long id : ids) {
                sql += "?,";
                pram.add(id);
            }
            sql = sql.substring(0, sql.length() - 1);
            sql += " ) group by to_char(begin_time, 'HH24') ";
//            return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LinkStatsAvg.class), ids);
            return eeq.sqlQuery(sql, LinkStatsAvg.class, pram);
        }
    }

    public Page<LinkStats> queryByGeometry(PGgeometry geometry, Boolean all, Page<LinkStats> page) {
        if (all) {
            String sql = " select #{col} from " + TABLE_NAME + " ls left join matsim_link ml on ls.link_id = ml.id where ls.deleted = 0 ";
//            Long total = jdbcTemplate.queryForObject(sql.replace("#{col}", "count(1)"), Long.class);
            List<Long> total = eeq.sqlQuery(sql.replace("#{col}", "count(1)"), Long.class);
//            List<LinkStats> data = jdbcTemplate.query(sql.replace("#{col}", "ls.*"), new BeanPropertyRowMapper<>(LinkStats.class));
            List<LinkStats> data = eeq.sqlQuery(sql.replace("#{col}", "ls.*"), LinkStats.class);
            return page.build(data, total.get(0));
        } else {
            List<Object> args = new ArrayList<>();
            args.add(geometry);
            String sql = " select #{col} from " + TABLE_NAME + " ls left join matsim_link ml on ls.link_id = ml.id where st_intersects(?, ml.geom) and ls.deleted = 0";

            Map<String, Object> param = page.getParam();
            if (param.get("type") != null && !"".equals(param.get("type"))) {
                sql += " and ls.type = ? ";
                args.add(param.get("type"));
            }
            if (param.get("beginTime") != null) {
                sql += " and ls.begin_time >= ? ";
                args.add(param.get("beginTime"));
            }
            if (param.get("endTime") != null) {
                sql += " and ls.end_time <= ? ";
                args.add(param.get("endTime"));
            }

//            Long total = jdbcTemplate.queryForObject(sql.replace("#{col}", "count(1)"), Long.class, args.toArray());
            List<Long> total = eeq.sqlQuery(sql.replace("#{col}", "count(1)"), Long.class, args);
            args.add(page.getPageSize());
            args.add(page.getOffset());

//            List<LinkStats> data = jdbcTemplate.query(sql.replace("#{col}", "ls.*") + LIMIT_SQL,
//                    new BeanPropertyRowMapper<>(LinkStats.class),
//                    args.toArray()
//            );
            List<LinkStats> data = eeq.sqlQuery(sql.replace("#{col}", "ls.*") + LIMIT_SQL, LinkStats.class, args);
            return page.build(data, total.get(0));
        }
    }

    public Page<LinkStats> queryByLinkId(String linkId, Page<LinkStats> page) {
        String sql = " select #{col} from " + TABLE_NAME + " ls where deleted = 0 and link_id = ? ";

        List<Object> args = new ArrayList<>();
        args.add(linkId);

        Map<String, Object> param = page.getParam();
        if (param.get("type") != null && !"".equals(param.get("type"))) {
            sql += " and ls.type = ? ";
            args.add(param.get("type"));
        }
        if (param.get("beginTime") != null) {
            sql += " and ls.begin_time >= ? ";
            args.add(param.get("beginTime"));
        }
        if (param.get("endTime") != null) {
            sql += " and ls.end_time <= ? ";
            args.add(param.get("endTime"));
        }

//        Long total = jdbcTemplate.queryForObject(sql.replace("#{col}", "count(1)"), Long.class, args.toArray());
        List<Long> total = eeq.sqlQuery(sql.replace("#{col}", "count(1)"), Long.class, args);

        sql += " order by update_time desc ";
        args.add(page.getPageSize());
        args.add(page.getOffset());

        List<LinkStats> data = eeq.sqlQuery(sql.replace("#{col}", BASE_FIELD) + LIMIT_SQL, LinkStats.class, args);

//        List<LinkStats> data = jdbcTemplate.query(sql.replace("#{col}", BASE_FIELD) + LIMIT_SQL,
//                new BeanPropertyRowMapper<>(),
//                args.toArray());
        return page.build(data, total.get(0));
    }

    public boolean delete(Long id) {
        long row = eeq.deletable(LinkStats.class).where(s -> s.id().eq(id)).executeRows();
//        int row = jdbcTemplate.update(" update " + TABLE_NAME + " set deleted = 1, version = version + 1, update_time = now() where id = ? ", id);
        return row > 0;
    }

    public static List<Object[]> genArgs(List<LinkStats> list) {
        List<Object[]> result = new ArrayList<>();
        list.forEach(stats -> {
            Object[] args = new Object[]{
                    stats.getId(),
                    stats.getLinkId(),
                    stats.getWayId(),
                    stats.getBeginTime(),
                    stats.getEndTime(),
                    stats.getType(),
                    stats.getPcuH(),
                    stats.getScar(),
                    stats.getStruck(),
                    stats.getMcar(),
                    stats.getMtruck(),
                    stats.getLcar(),
                    stats.getLtruck(),
                    stats.getVideo(),
                    stats.getIsTwoWay(),
                    stats.getX(),
                    stats.getY(),
                    stats.getRemark(),
                    stats.getIpAddr(),
                    1,
                    0,
                    new Date(),
                    new Date()
            };
            result.add(args);
        });
        return result;
    }

}
