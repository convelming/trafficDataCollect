package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.Crossroads;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CrossroadsMapper {

    private static final String TABLE_NAME = " crossroads ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public Page<Crossroads> list(Page<Crossroads> page) {
        List<Crossroads> data = eeq.queryable(Crossroads.class).limit(page.getOffset(), page.getPageSize()).toList();
        long total = eeq.queryable(Crossroads.class).count();
        return page.build(data, total);
    }

    public boolean insert(Crossroads cossroads) {
        cossroads.setId(idUtil.getId(TABLE_NAME));
        cossroads.setVersion(1);
        cossroads.setDeleted(0L);
        long row = eeq.insertable(cossroads).executeRows();
        return row > 0;
    }

    public boolean delete(Long id) {
        long row = eeq.deletable(Crossroads.class).where(s -> s.id().eq(id)).executeRows();
//        int row = jdbcTemplate.update(" update " + TABLE_NAME + " set deleted = 1, version = version + 1, update_time = now() where id = ? ", id);
        return row > 0;
    }

    public Crossroads selectById(Long id) {
        return eeq.queryable(Crossroads.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    public boolean updateStatus(Long id, Integer status) {
        return eeq.updatable(Crossroads.class).setColumns(t -> t.status().set(status)).where(t -> t.id().eq(id)).executeRows() == 1;
    }

    public int saveLines(Crossroads cossroads) {
        return (int) eeq.updatable(Crossroads.class).setColumns(t -> {
            t.lines().set(cossroads.getLines());
            t.version().set(cossroads.getVersion());
            t.updateTime().set(cossroads.getUpdateTime());
        }).where(t -> t.id().eq(cossroads.getId())).executeRows();
    }

}
