package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.MatsimNodeProxy;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;

@Data
@NoArgsConstructor
@AllArgsConstructor

@Table("matsim_node")
@EntityProxy
public class MatsimNode implements ProxyEntityAvailable<MatsimNode, MatsimNodeProxy> {

    /**
     * id
     */
    private String id;

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
