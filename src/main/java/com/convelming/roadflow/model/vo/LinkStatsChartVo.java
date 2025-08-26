package com.convelming.roadflow.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsChartVo {

    private String origid;
    private String linkId;
    private String service;
    private Double saturation;
    private String style;
    private Double[] fromxy;
    private Double[] toxy;
    private Boolean oneWay = false;
}
