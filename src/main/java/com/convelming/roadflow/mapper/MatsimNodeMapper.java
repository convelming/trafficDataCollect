package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.MatsimNode;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import net.postgis.jdbc.PGgeometry;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Repository
public class MatsimNodeMapper {

    private final String TABLE_NAME = " matsim_node ";

    private final String FIELD = "id, srid, x, y, geom";
    //    private final String INSERT_SQL = "insert into " + TABLE_NAME + "(srid, x, y, geom) values (?, ?, ?, ?)";
    private final String INSERT_SQL = "insert into " + TABLE_NAME + "(" + FIELD + ") values (?, ?, ?, ?, ?)";

    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    EasyEntityQuery eeq;

    public boolean insert(MatsimNode node) {
        long row = eeq.insertable(node).executeRows();
        return row > 0;
    }

    public boolean deleteById(String id) {
        long row = eeq.deletable(MatsimNode.class).where(t -> t.id().eq(id)).executeRows();
        return row > 0;
    }

    public MatsimNode selectById(String id) {
        return eeq.queryable(MatsimNode.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    public List<MatsimNode> selectByIds(Collection<String> ids) {

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        return eeq.queryable(MatsimNode.class).where(t -> t.id().in(ids)).toList();
    }

    public List<MatsimNode> selectIntersects(PGgeometry ggeometry) {
        if (ggeometry == null) {
            return new ArrayList<>();
        }
        String sql = "select * from " + TABLE_NAME + " where ST_Intersects(?, geom)";
        return eeq.sqlQuery(sql, MatsimNode.class, List.of(ggeometry));
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
