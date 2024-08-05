package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.convelming.roadflow.model.proxy.CossroadsProxy;
import com.convelming.roadflow.model.proxy.CossroadsStatsProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelTarget("cossroads_stats")

@Table("cossroads_stats")
@EntityProxy
public class CossroadsStats implements ProxyEntityAvailable<CossroadsStats, CossroadsStatsProxy> {

    @Column(primaryKey = true)
    private Long id;

    private Long cossroadsId;

    private String inLink;

    private String outLink;

    private Double pcuH;

    private String pcuDetail;

    private Double count;

    /**
     * 默认详情 小客车，小货车，中客车，中货车，大客车，大货车
     */
    public static String DEFAULT_DETAIL = "[" +
            "{\"type\":\"小客车\",\"num\":0,\"ratio\":1}," +
            "{\"type\":\"小货车\",\"num\":0,\"ratio\":1}," +
            "{\"type\":\"中客车\",\"num\":0,\"ratio\":1.5}," +
            "{\"type\":\"中货车\",\"num\":0,\"ratio\":1.5}," +
            "{\"type\":\"大客车\",\"num\":0,\"ratio\":2}," +
            "{\"type\":\"大货车\",\"num\":0,\"ratio\":2}" +
            "]";

}
