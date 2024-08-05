package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.CossroadsStats;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CossroadsStatsMapper {
    private static final String TABLE_NAME = " cossroads_stats ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    /**
     * 新增十字路流量
     * @param stats 流量数据
     */
    public boolean insert(CossroadsStats stats) {
        stats.setId(idUtil.getId(TABLE_NAME));
        return eeq.insertable(stats).executeRows() > 0;
    }

    /**
     * 批量新增十字路流量
     * @param list 流量数据
     */
    public boolean batchInsert(List<CossroadsStats> list) {
        for(CossroadsStats stats : list){
            stats.setId(idUtil.getId(TABLE_NAME));
        }
        return eeq.insertable(list).batch().executeRows() == list.size();
    }

    /**
     * 删除十字路流量数据
     * @param cossroadsId 十字路id
     */
    public boolean deleteByCossroadsId(Long cossroadsId) {
        return eeq.deletable(CossroadsStats.class).where(t -> t.cossroadsId().eq(cossroadsId)).executeRows() > 0;
    }

    /**
     * 查询十字路流量数据
     * @param cossroadsId 十字路id
     */
    public List<CossroadsStats> selectByCossroadsId(Long cossroadsId){
        return eeq.queryable(CossroadsStats.class).where(t -> t.cossroadsId().eq(cossroadsId)).toList();
    }


}
