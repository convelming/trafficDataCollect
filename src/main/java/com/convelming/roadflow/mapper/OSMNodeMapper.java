package com.convelming.roadflow.mapper;


import com.convelming.roadflow.model.OSMNode;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OSMNodeMapper {

    private static final String TABLE_NAME = " osm_node ";

    private static final String INSERT_SQL = "insert into " + TABLE_NAME + "(id, version, timestamp, uid, \"user\", changeset, lat, lon, x, y, geom4326, geom3857, other) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private EasyEntityQuery eeq;

    public boolean insert(OSMNode node) {
        long row = eeq.insertable(node).executeRows();
        return row > 0;
    }

    public boolean deleteById(Long id) {
        long row = eeq.deletable(OSMNode.class).where(t -> t.id().eq(id)).executeRows();
        return row > 0;
    }

    public OSMNode selectById(Long id) {
        return eeq.queryable(OSMNode.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    public boolean batchInsert(List<OSMNode> nodes) {
        List<Object[]> args = new ArrayList<>();
        for (OSMNode node : nodes) {
            args.add(genArgs(node));
        }
        jdbcTemplate.batchUpdate(INSERT_SQL, args);
        return true;
    }

    private Object[] genArgs(OSMNode node) {
        return new Object[]{
                node.getId(),
                node.getVersion(),
                node.getTimestamp(),
                node.getUid(),
                node.getUser(),
                node.getChangeset(),
                node.getLat(),
                node.getLon(),
                node.getX(),
                node.getY(),
                node.getGeom4326(),
                node.getGeom3857(),
                node.getOther()
        };
    }


}
