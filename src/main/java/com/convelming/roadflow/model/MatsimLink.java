package com.convelming.roadflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.postgis.jdbc.PGgeometry;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatsimLink {

    /**
     * id
     */
    private Long id;

    /**
     * 空间坐标编码
     */
    private Integer srid;

    /**
     * 起始点
     */
    private Long fromNode;

    /**
     * 结束点
     */
    private Long toNode;

    /**
     * 长度
     */
    private Double length;

    /**
     * 自由速度
     */
    private Double freespeed;

    /**
     * 容量
     */
    private Double capacity;

    /**
     * 空间坐标信息
     */
    @JsonIgnore
    private PGgeometry geom;

    /**
     * osm way id
     */
    private Long origid;

    /**
     * osm way highway
     */
    private String type;

    /**
     * 中心点
     */
    @JsonIgnore
    private PGgeometry center;

    private String statsType;

    private Double[] toxy;

    private Double[] fromxy;

    public MatsimLink(MatsimLink link){
        BeanUtils.copyProperties(link, this);
    }

}
