package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.mapper.OSMWayMapper;
import com.convelming.roadflow.model.vo.OSMWayVo;
import com.convelming.roadflow.service.OSMWayService;
import com.convelming.roadflow.util.CacheUtil;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import net.postgis.jdbc.PGgeometry;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.convelming.roadflow.util.CacheUtil.ALL_OSM_WAY_KEY;
import static com.convelming.roadflow.util.CacheUtil.DAY;

@Service
public class OSMWayServiceImpl implements OSMWayService {

    @Resource
    private OSMWayMapper osmWayMapper;

    @Resource
    private CacheUtil cacheUtil;

    @Override
    public List<OSMWayVo> getGeomjson(double[][] xyarr, boolean isAll) {
        List<OSMWayVo> body;
        if (isAll) {
            body =  (List<OSMWayVo>) cacheUtil.get(ALL_OSM_WAY_KEY);
            if(body == null){
                body = osmWayMapper.queryAllGeojson();
                cacheUtil.put(ALL_OSM_WAY_KEY, body, DAY);
            }
        } else {
            PGgeometry geometry = GeomUtil.genPolygon(xyarr, GeomUtil.MKT);
            body = osmWayMapper.queryByPolygonGeojson(geometry);
        }
        return body;
    }
}
