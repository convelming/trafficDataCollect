package com.convelming.roadflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatsimNode {

    /**
     * id
     */
    private Long id;

    /**
     * x 坐标
     */
    private Double x;

    /**
     * y 坐标
     */
    private Double y;

    /**
     * 空间坐标编码
     */
    private Integer srid;

    /**
     * 点
     */
    private PGgeometry geom;


}
