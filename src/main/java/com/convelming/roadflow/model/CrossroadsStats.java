package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.Excel;
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

    @Excel(name = "进入linkId")
    private String inLink;

    @Excel(name = "离开linkId")
    private String outLink;

    @Excel(name = "pcu/h", width = 30, isImportField = "wayId")
    private Double pcuH;

    @Excel(name = "小客车")
    private int car;

    @Excel(name = "大客车")
    private int bus;

    @Excel(name = "小货车")
    private int van;

    @Excel(name = "大货车")
    private int truck;

    @Excel(name = "方向")
    private String resultId;

    private String pcuDetail;

    @Excel(name = "总数")
    private int count;

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
