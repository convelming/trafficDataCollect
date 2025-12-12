package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.LinkProject;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class LinkProjectMapper {

    private static final String TABLE_NAME = " link_project ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public boolean insert(LinkProject linkProject) {
        linkProject.setId(idUtil.getId(TABLE_NAME));
        linkProject.setCreateTime(new Date());
        return eeq.insertable(linkProject).executeRows() > 0;
    }

    public boolean update(LinkProject linkProject) {
        return eeq.updatable(linkProject).executeRows() > 0;
    }

    public boolean delete(Long id) {
        return eeq.deletable(LinkProject.class).where(t -> t.id().eq(id)).executeRows() > 0;
    }

    public Page<LinkProject> page(Page<LinkProject> page) {
        Map<String, Object> params = page.getParam();
        List<LinkProject> data = eeq.queryable(LinkProject.class).where(
                t -> {
                    t.name().like(params.get("name") != null, params.get("name").toString());
                    t.creator().like(params.get("creator") != null, params.get("creator").toString());
                }
        ).toList();
        long total = eeq.queryable(LinkProject.class).where(
                t -> {
                    t.name().like(params.get("name") != null, params.get("name").toString());
                    t.creator().like(params.get("creator") != null, params.get("creator").toString());
                }).count();
        return page.build(data, total);
    }

    public LinkProject selectById(Long id) {
        return eeq.queryable(LinkProject.class).where(t -> t.id().eq(id)).singleOrNull();
    }

    public List<LinkProject> list(LinkProject linkProject) {
        return eeq.queryable(LinkProject.class).toList();
    }

}
