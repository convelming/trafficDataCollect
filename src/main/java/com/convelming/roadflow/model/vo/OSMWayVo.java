package com.convelming.roadflow.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OSMWayVo {

    private Long id;

    private String geom;

//    public JSONObject getGeom(){
//        return JSONObject.parseObject(this.geom);
//    }

}
