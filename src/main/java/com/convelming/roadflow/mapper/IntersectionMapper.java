package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.Intersection;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IntersectionMapper {
    private static final String TABLE_NAME = " intersection ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public Page<Intersection> list(Page<Intersection> page) {
        List<Intersection> data = eeq.queryable(Intersection.class).orderBy(t -> t.id().desc()).limit(page.getOffset(), page.getPageSize()).toList();
        long total = eeq.queryable(Intersection.class).count();
        return page.build(data, total);
    }


    public Intersection selectById(Long id) {
        return eeq.queryable(Intersection.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    /**
     * 新增十字路
     *
     * @param intersection
     */
    public boolean insert(Intersection intersection) {
        intersection.setId(idUtil.getId(TABLE_NAME));
        return eeq.insertable(intersection).executeRows() > 0;
    }

    /**
     * 修改十字路流量
     *
     * @param intersection
     */
    public boolean updateById(Intersection intersection) {
        return eeq.updatable(intersection).executeRows() > 0;
    }

    public boolean updateStatus(Long id, Integer status) {
        return eeq.updatable(Intersection.class).setColumns(i -> i.status().set(status)).where(t -> t.id().eq(id)).executeRows() > 0;
    }

    /**
     * 删除
     *
     * @param id id
     */
    public boolean delete(Long id) {
        return eeq.deletable(Intersection.class).whereById(id).executeRows() > 0;
    }

}
