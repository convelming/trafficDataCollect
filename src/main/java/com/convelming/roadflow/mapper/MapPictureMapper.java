package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.Crossroads;
import com.convelming.roadflow.model.MapPicture;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class MapPictureMapper {

    private static final String TABLE_NAME = " map_picture ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public Page<MapPicture> page(Page<MapPicture> page) {
        List<MapPicture> data = eeq.queryable(MapPicture.class)
                .orderBy(t -> t.id().desc()).limit(page.getOffset(), page.getPageSize()).toList();
        long total = eeq.queryable(Crossroads.class)
                .count();
        data.forEach(mp -> mp.setUrl(Constant.FILE_DOWNLOAD_API + mp.getPath()));
        return page.build(data, total);
    }

    public Collection<MapPicture> all() {
        Collection<MapPicture> list = eeq.queryable(MapPicture.class).toList();
        list.forEach(mp -> mp.setUrl(Constant.FILE_DOWNLOAD_API + mp.getPath()));
        return list;
    }

    public Collection<MapPicture> list(Map<String, Object> param) {
        Collection<MapPicture> list = eeq.queryable(MapPicture.class)
                .where(t -> {
//                    t.path().like(param.get("name") != null && !"".equals(param.get("name")), param.get("name").toString());
                    if (param.get("beginTime") != null) {
                        t.dataTime().gt((Date) param.get("beginTime"));
                    }
                    if (param.get("endTime") != null) {
                        t.dataTime().lt(DateUtils.addDays((Date) param.get("endTime"), 1));
                    }
                })
                .toList();
        list.forEach(mp -> mp.setUrl(Constant.FILE_DOWNLOAD_API + mp.getPath()));
        return list;
    }

    public MapPicture selectById(Long id) {
        return eeq.queryable(MapPicture.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    public long batchInsert(List<MapPicture> list) {
        list.forEach(mp -> mp.setId(idUtil.getId(TABLE_NAME)));
        return eeq.insertable(list).batch().executeRows();
    }

    public long batchDeleteById(Collection<Long> ids) {
        return eeq.deletable(MapPicture.class).whereByIds(ids).executeRows();
    }

    public long deleteByPath(String path) {
        return eeq.deletable(MapPicture.class).where(t -> t.path().likeMatchLeft(path)).executeRows();
    }

}
