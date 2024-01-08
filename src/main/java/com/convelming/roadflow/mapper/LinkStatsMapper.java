package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.util.IdUtil;
import jakarta.annotation.Resource;
import net.postgis.jdbc.PGgeometry;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LinkStatsMapper {

    private static final String TABLE_NAME = " link_stats ";

    private static final String BASE_FIELD = " id, link_id, way_id, begin_time, end_time, \"type\", pcu_h, scar, struck, mcar, mtruck, lcar, ltruck, video, is_two_way, x, y, remark, ip_addr, version, deleted, create_time, update_time ";

    private static final String INSERT_SQL = " insert into " + TABLE_NAME + " ( " + BASE_FIELD + " ) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ";

    private static final String LIMIT_SQL = " limit ? offset ? ";

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private IdUtil idUtil;

    public boolean insert(LinkStats stats) {
        stats.setId(idUtil.getId(TABLE_NAME));

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

        int row = jdbcTemplate.update(INSERT_SQL, args);
        return row > 0;
    }

    public boolean update(LinkStats stats) {
        String sql = " update " + TABLE_NAME + " set ";

        sql += " link_id = ?, ";
        sql += " way_id = ?, ";
        sql += " begin_time = ?, ";
        sql += " end_time = ?, ";
        sql += " \"type\" = ?, ";
        sql += " pcu_h = ?, ";
        sql += " scar = ?, ";
        sql += " struck = ?, ";
        sql += " mcar = ?, ";
        sql += " mtruck = ?, ";
        sql += " lcar = ?, ";
        sql += " ltruck = ?, ";
        sql += " remark = ?, ";
        sql += " ip_addr = ?, ";
        sql += " version = version + 1, ";
        sql += " video = ?, ";
        sql += " is_two_way = ?, ";
        sql += " update_time = now() ";

        sql += " where id = ? ";

        int row = jdbcTemplate.update(sql, new Object[]{
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
                stats.getRemark(),
                stats.getIpAddr(),
                stats.getVideo(),
                stats.getIsTwoWay(),
                stats.getId()
        });

        return row > 0;
    }

    public LinkStats selectById(Long id) {
        return jdbcTemplate.queryForObject(" select * from " + TABLE_NAME + " where id = ? and deleted = 0", new BeanPropertyRowMapper<>(LinkStats.class), id);
    }

    public List<LinkStats> queryAllMaker(Date beginTime, Date endTime, String type) {
        String sql = " select distinct link_id, x, y, max(pcu_h) \"pcu_h\", string_agg(distinct type, ',') as \"type\" from " + TABLE_NAME + " ls where ls.deleted = 0 ";
        List<Object> args = new ArrayList<>();
        if (type != null && !"".equals(type)) {
            sql += " and ls.type = ? ";
            args.add(type);
        }
        if (beginTime != null) {
            sql += " and ls.begin_time >= ? ";
            args.add(beginTime);
        }
        if (endTime != null) {
            sql += " and ls.end_time <= ? ";
            args.add(endTime);
        }
        sql += " group by link_id, x, y ";

        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LinkStats.class), args.toArray());
    }

    public List<LinkStats> queryByIds(Collection<Long> ids) {
//        StringBuilder sql = new StringBuilder(" select " + BASE_FIELD + " from " + TABLE_NAME + " where id in (");
        StringBuilder sql = new StringBuilder("select ls.*, st_asewkt(ml.geom) \"linkLineString\", st_asewkt(ow.geom3857) \"wayLineString\" from link_stats ls left join matsim_link ml on ls.link_id=ml.id left join osm_way ow on ml.origid=ow.id  where ls.id in (");
        for (int i = 0, len = ids.size(); i < len; i++) {
            if (i == len - 1) {
                sql.append("?");
            } else {
                sql.append("?,");
            }
        }
        sql.append(") and deleted = 0");
        return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(LinkStats.class), ids.toArray());
    }

    public Page<LinkStats> queryByGeometry(PGgeometry geometry, Boolean all, Page<LinkStats> page) {
        if (all) {
            String sql = " select #{col} from " + TABLE_NAME + " ls left join matsim_link ml on ls.link_id = ml.id where ls.deleted = 0 ";
            Long total = jdbcTemplate.queryForObject(sql.replace("#{col}", "count(1)"), Long.class);
            List<LinkStats> data = jdbcTemplate.query(sql.replace("#{col}", "ls.*"), new BeanPropertyRowMapper<>(LinkStats.class));
            return page.build(data, total);
        } else {
            List<Object> args = new ArrayList<>();
            args.add(geometry);
            String sql = " select #{col} from " + TABLE_NAME + " ls left join matsim_link ml on ls.link_id = ml.id where st_intersects(?, ml.geom) and ls.deleted = 0 ";

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

            Long total = jdbcTemplate.queryForObject(sql.replace("#{col}", "count(1)"), Long.class, args.toArray());
            args.add(page.getPageSize());
            args.add(page.getOffset());
            List<LinkStats> data = jdbcTemplate.query(sql.replace("#{col}", "ls.*") + LIMIT_SQL,
                    new BeanPropertyRowMapper<>(LinkStats.class),
                    args.toArray()
            );
            return page.build(data, total);
        }
    }

    public Page<LinkStats> queryByLinkId(Long linkId, Page<LinkStats> page) {
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

        Long total = jdbcTemplate.queryForObject(sql.replace("#{col}", "count(1)"), Long.class, args.toArray());

        sql += " order by update_time desc ";
        args.add(page.getPageSize());
        args.add(page.getOffset());
        List<LinkStats> data = jdbcTemplate.query(sql.replace("#{col}", BASE_FIELD) + LIMIT_SQL,
                new BeanPropertyRowMapper<>(LinkStats.class),
                args.toArray());
        return page.build(data, total);
    }

    public boolean delete(Long id) {
        int row = jdbcTemplate.update(" update " + TABLE_NAME + " set deleted = 1, version = version + 1, update_time = now() where id = ? ", id);
        return row > 0;
    }

}
