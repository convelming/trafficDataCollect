package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.LinkProjectStats;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class LinkProjectStatsMapper {

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    private static final String TABLE_NAME = " link_project_stats ";

    public boolean batchInsert(List<LinkProjectStats> list) {
        list.forEach(e -> {
            e.setId(idUtil.getId(TABLE_NAME));
        });
        return eeq.insertable(list).executeRows() > 0;
    }

    public boolean batchDelete(Collection<Long> ids) {
        return eeq.deletable(LinkProjectStats.class).where(t -> {
            t.id().in(ids);
        }).executeRows() > 0;
    }

    public List<LinkProjectStats> query(Long[] projectIds, String[] linkIds) {
        return eeq.queryable(LinkProjectStats.class).where(t -> {
            t.projectId().in(projectIds != null && projectIds.length > 0, projectIds);
            t.linkId().in(linkIds != null && linkIds.length > 0, linkIds);
        }).toList();
    }

    public List<LinkProjectStats> queryExistData(List<String> linkIds, Long projectId) {
        return eeq.queryable(LinkProjectStats.class).where(t -> {
            t.linkId().in(linkIds);
            t.projectId().eq(projectId);
        }).select("id").toList();
    }


}
