package com.convelming.roadflow.service;

import com.convelming.roadflow.model.vo.OSMWayVo;

import java.util.List;

public interface OSMWayService {

    List<OSMWayVo> getGeomjson(double[][] xyarr, boolean isAll);



}
