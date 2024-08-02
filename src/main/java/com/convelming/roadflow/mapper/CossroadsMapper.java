package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.Cossroads;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class CossroadsMapper {

    private static final String TABLE_NAME = " cossroads ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public boolean insert(Cossroads cossroads) {
        cossroads.setId(idUtil.getId(TABLE_NAME));
        cossroads.setVersion(1);
        cossroads.setDeleted(0L);
        long row = eeq.insertable(cossroads).executeRows();
        return row > 0;
    }

    public boolean delete(Long id) {
        long row = eeq.deletable(Cossroads.class).where(s -> s.id().eq(id)).executeRows();
//        int row = jdbcTemplate.update(" update " + TABLE_NAME + " set deleted = 1, version = version + 1, update_time = now() where id = ? ", id);
        return row > 0;
    }


}
