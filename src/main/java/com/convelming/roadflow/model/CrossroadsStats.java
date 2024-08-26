package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.convelming.roadflow.model.proxy.CrossroadsStatsProxy;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelTarget("crossroads_stats")

@Table("crossroads_stats")
@EntityProxy
public class CrossroadsStats implements ProxyEntityAvailable<CrossroadsStats, CrossroadsStatsProxy> {

    @Column(primaryKey = true)
    private Long id;

    private Long cossroadsId;

    private String inLink;

    private String outLink;

    private Double pcuH;

    private String pcuDetail;

    private Double count;

    @JsonIgnore
    private Long deleted;

    public static String DETAIL_NUM = "num";
    public static String DETAIL_RATIO = "ratio";
    /**
     * 默认详情 小客车，小货车，中客车，中货车，大客车，大货车
     */
    public static String DEFAULT_DETAIL = "[" +
            "{\"type\":\"小客车\",\"" + DETAIL_NUM + "\":0,\"" + DETAIL_RATIO + "\":1}," +
            "{\"type\":\"小货车\",\"" + DETAIL_NUM + "\":0,\"" + DETAIL_RATIO + "\":1}," +
            "{\"type\":\"中客车\",\"" + DETAIL_NUM + "\":0,\"" + DETAIL_RATIO + "\":1.5}," +
            "{\"type\":\"中货车\",\"" + DETAIL_NUM + "\":0,\"" + DETAIL_RATIO + "\":1.5}," +
            "{\"type\":\"大客车\",\"" + DETAIL_NUM + "\":0,\"" + DETAIL_RATIO + "\":2}," +
            "{\"type\":\"大货车\",\"" + DETAIL_NUM + "\":0,\"" + DETAIL_RATIO + "\":2}" +
            "]";
}
