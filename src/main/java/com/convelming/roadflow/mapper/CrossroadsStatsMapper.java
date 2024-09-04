package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

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
        return eeq.updatable(CrossroadsStats.class).where(t -> t.id().eq(stats.getId())).executeRows() > 0;
//        return eeq.updatable(stats).executeRows() > 0;
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
     * 删除十字路流量数据
     *
     * @param cossroadsId 十字路id
     */
    public boolean deleteByCossroadsId(Long cossroadsId) {
        return eeq.updatable(CrossroadsStats.class)
                .setColumns(t -> t.deleted().set(1L))
                .where(t -> t.cossroadsId().eq(cossroadsId))
                .executeRows() > 0;
//        return eeq.deletable(CrossroadsStats.class).where(t -> t.cossroadsId().eq(cossroadsId)).executeRows() > 0;
    }

    /**
     * 查询十字路流量数据
     *
     * @param cossroadsId 十字路id
     */
    public List<CrossroadsStats> selectByCossroadsId(Long cossroadsId) {
        return eeq.queryable(CrossroadsStats.class).where(t -> {t.cossroadsId().eq(cossroadsId);t.deleted().eq(0L);}).toList();
    }

    /**
     * 统计十字路进出link是否已被添加
     * @param cossroadsId 十字路id
     * @param inLink      inlink
     * @param outLink     outlink
     * @return
     */
    public long countCossroadsInOutLink(Long cossroadsId, String inLink, String outLink) {
        return eeq.queryable(CrossroadsStats.class)
                .where(t -> {
                    t.cossroadsId().eq(cossroadsId);
                    t.inLink().eq(inLink);
                    t.outLink().eq(outLink);
                    t.deleted().eq(0L);
                })
                .count();
    }

    /**
     * 统计inoutlink是否在十字路中
     * @param cossroadsId 十字路id
     * @param inLink      inlink
     * @param outLink     outlink
     * @return
     */
    public long countCossroadsInOrOutLink(Long cossroadsId, String inLink, String outLink) {
        return eeq.queryable(CrossroadsStats.class)
                .where(t -> {
                    t.cossroadsId().eq(cossroadsId);
                    t.inLink().eq(inLink);
                    t.outLink().eq(outLink);
                })
                .count();
    }


}
