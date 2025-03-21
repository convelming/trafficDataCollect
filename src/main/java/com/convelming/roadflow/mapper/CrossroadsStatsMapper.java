package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.MatsimNode;
import com.convelming.roadflow.model.proxy.CrossroadsStatsProxy;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CrossroadsStatsMapper {
    private static final String TABLE_NAME = " crossroads_stats ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    /**
     * 新增十字路流量
     *
     * @param stats 流量数据
     */
    public boolean insert(CrossroadsStats stats) {
        stats.setId(idUtil.getId(TABLE_NAME));
        return eeq.insertable(stats).executeRows() > 0;
    }

    /**
     * 修改十字路流量
     *
     * @param stats 流量数据
     */
    public boolean updateById(CrossroadsStats stats) {
        return eeq.updatable(stats).executeRows() > 0;
//        return eeq.updatable(stats).executeRows() > 0;
    }

    /**
     * 批量修改
     */
    public boolean batchUpdate(Collection<CrossroadsStats> list) {
        return eeq.updatable(list).executeRows() > 0;
    }

    /**
     * 批量新增十字路流量
     *
     * @param list 流量数据
     */
    public boolean batchInsert(List<CrossroadsStats> list) {
        for (CrossroadsStats stats : list) {
            stats.setId(idUtil.getId(TABLE_NAME));
        }
        return eeq.insertable(list).batch().executeRows() == list.size();
    }

    /**
     * 删除十字路流量数据，物理删除
     *
     * @param cossroadsId 十字路id
     */
    public boolean deleteByCrossroadsId(Long cossroadsId) {
        return eeq.deletable(CrossroadsStats.class)
                .where(t -> t.crossroadsId().eq(cossroadsId))
                .allowDeleteStatement(true).executeRows() > 0; // 物理删除
//        return eeq.deletable(CrossroadsStats.class).where(t -> t.cossroadsId().eq(cossroadsId)).executeRows() > 0;
    }

    /**
     * 删除十字路流量数据
     *
     * @param id crossroadStatsId
     */
    public boolean deleteById(Long id) {
        return eeq.deletable(CrossroadsStats.class)
                .where(t -> t.id().eq(id))
                .executeRows() > 0;
//        return eeq.deletable(CrossroadsStats.class).where(t -> t.cossroadsId().eq(cossroadsId)).executeRows() > 0;
    }

    /**
     * 删除十字路流量数据
     *
     * @param id crossroadStatsId
     */
    public boolean deleteByIds(Long[] id) {
        return eeq.updatable(CrossroadsStats.class)
                .setColumns(t -> t.deleted().set(1L))
                .where(t -> t.id().in(id))
                .executeRows() > 0;
//        return eeq.deletable(CrossroadsStats.class).where(t -> t.cossroadsId().eq(cossroadsId)).executeRows() > 0;
    }

    /**
     * 查询十字路流量数据
     *
     * @param crossroadsId 十字路id
     */
    public List<CrossroadsStats> selectByCrossroadsId(Long crossroadsId) {
        List<CrossroadsStats> list = eeq.queryable(CrossroadsStats.class).where(t -> {
            t.crossroadsId().eq(crossroadsId);
            t.deleted().eq(0L);
        }).orderBy(t -> t.id().asc()).toList();

        Set<String> linksId = new HashSet<>();
        linksId.addAll(list.stream().map(CrossroadsStats::getInLink).toList());
        linksId.addAll(list.stream().map(CrossroadsStats::getOutLink).toList());
        List<MatsimLink> links = eeq.queryable(MatsimLink.class).where(t -> t.id().in(linksId)).toList();

        Set<String> nodesId = new HashSet<>();
        for (MatsimLink link : links) {
            nodesId.add(link.getFromNode());
            nodesId.add(link.getToNode());
        }
        List<MatsimNode> nodes = eeq.queryable(MatsimNode.class).where(t -> t.id().in(nodesId)).toList();
        Map<String, MatsimNode> nodeMap = nodes.stream().collect(Collectors.toMap(MatsimNode::getId, (x -> x)));
        Map<String, MatsimLink> linkMap = links.stream().collect(Collectors.toMap(MatsimLink::getId, (x -> x)));
        for (MatsimLink link : links) {
            MatsimNode to = nodeMap.get(link.getToNode());
            MatsimNode from = nodeMap.get(link.getFromNode());
            link.setToxy(new Double[]{to.getX(), to.getY()});
            link.setFromxy(new Double[]{from.getX(), from.getY()});
        }
        list.forEach(stats -> {
            stats.setInLinkInfo(linkMap.get(stats.getInLink()));
            stats.setOutLinkInfo(linkMap.get(stats.getOutLink()));
        });
        return list;
    }

    /**
     * 查询十字路流量数据
     *
     * @param crossroadsId 十字路id
     */
    public List<CrossroadsStats> selectByCrossroadsIdAndPuchIsNotNull(Long crossroadsId) {
        return eeq.queryable(CrossroadsStats.class).where(t -> {
            t.crossroadsId().eq(crossroadsId);
            t.pcuH().isNotNull();
            t.deleted().eq(0L);
        }).orderBy(t -> t.id().asc()).toList();
    }

    /**
     * 统计十字路进出link是否已被添加
     *
     * @param crossroadsId 十字路id
     * @param inLink       inlink
     * @param outLink      outlink
     * @return
     */
    public long countCrossroadsInOutLink(Long crossroadsId, String inLink, String outLink) {
        return eeq.queryable(CrossroadsStats.class)
                .where(t -> {
                    t.crossroadsId().eq(crossroadsId);
                    t.inLink().eq(inLink);
                    t.outLink().eq(outLink);
                    t.deleted().eq(0L);
                })
                .count();
    }

    /**
     * 统计inoutlink是否在十字路中
     *
     * @param crossroadsId 十字路id
     * @param inLink       inlink
     * @param outLink      outlink
     * @return
     */
    public long countCrossroadsInOrOutLink(Long crossroadsId, String inLink, String outLink) {
        return eeq.queryable(CrossroadsStats.class)
                .where(t -> {
                    t.crossroadsId().eq(crossroadsId);
                    t.inLink().eq(inLink);
                    t.outLink().eq(outLink);
                })
                .count();
    }


}
