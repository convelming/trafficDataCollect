package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.Crossroads;
import com.convelming.roadflow.model.Intersection;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CrossroadsMapper {

    private static final String TABLE_NAME = " crossroads ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public Page<Crossroads> list(Page<Crossroads> page) {
        List<Crossroads> data = eeq.queryable(Crossroads.class)
                .where(t -> t.intersectionId().eq(page.getParam().get("intersectionId") != null, (Long) page.getParam().get("intersectionId")))
                .orderBy(t -> t.id().desc()).limit(page.getOffset(), page.getPageSize()).toList();

        Map<Long, String> maps = eeq.queryable(Intersection.class).where(t -> t.id().in(data.stream().map(Crossroads::getIntersectionId).collect(Collectors.toSet())))
                .toList().stream().collect(Collectors.toMap(Intersection::getId, Intersection::getName));
        data.forEach(d -> d.setName(maps.get(d.getIntersectionId())));

        long total = eeq.queryable(Crossroads.class)
                .where(t -> t.intersectionId().eq(page.getParam().get("intersectionId") != null, (Long) page.getParam().get("intersectionId")))
                .count();
        return page.build(data, total);
    }

    public boolean insert(Crossroads crossroads) {
        crossroads.setId(idUtil.getId(TABLE_NAME));
        crossroads.setVersion(1);
        crossroads.setDeleted(0L);
        long row = eeq.insertable(crossroads).executeRows();
        return row > 0;
    }

    public boolean updateById(Crossroads crossroads) {
        long row = eeq.updatable(crossroads).executeRows();
        return row > 0;
    }

    public Long countByIntersectionId(Long intersectionId) {
        return eeq.queryable(Crossroads.class).where(t -> t.intersectionId().eq(intersectionId)).count();
    }

    public boolean delete(Long id) {
        long row = eeq.deletable(Crossroads.class).where(s -> s.id().eq(id)).executeRows();
//        int row = jdbcTemplate.update(" update " + TABLE_NAME + " set deleted = 1, version = version + 1, update_time = now() where id = ? ", id);
        return row > 0;
    }

    public boolean deleteByIds(Long[] ids) {
        return 0 < eeq.deletable(Crossroads.class).where(t -> t.id().in(ids)).executeRows();
    }

    public Crossroads selectById(Long id) {
        return eeq.queryable(Crossroads.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    public boolean updateStatus(Long id, Integer status) {
        return eeq.updatable(Crossroads.class).setColumns(t -> t.status().set(status)).where(t -> t.id().eq(id)).executeRows() == 1;
    }

    public int saveLines(Crossroads cossroads) {
        return (int) eeq.updatable(cossroads).executeRows();
    }

}
