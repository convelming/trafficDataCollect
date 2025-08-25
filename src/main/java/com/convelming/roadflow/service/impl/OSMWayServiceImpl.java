package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.mapper.OSMWayMapper;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.OSMWay;
import com.convelming.roadflow.model.vo.OSMWayVo;
import com.convelming.roadflow.service.OSMWayService;
import com.convelming.roadflow.util.CacheUtil;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import net.postgis.jdbc.PGgeometry;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.convelming.roadflow.util.CacheUtil.ALL_OSM_WAY_KEY;
import static com.convelming.roadflow.util.CacheUtil.DAY;

@Service
public class OSMWayServiceImpl implements OSMWayService {

    @Resource
    private OSMWayMapper osmWayMapper;

    @Resource
    private MatsimLinkMapper matsimLinkMapper;

    @Resource
    private CacheUtil cacheUtil;

    @Override
    public List<OSMWayVo> getGeomjson(double[][] xyarr, boolean isAll) {
        List<OSMWayVo> body;
        if (isAll) {
            body = (List<OSMWayVo>) cacheUtil.get(ALL_OSM_WAY_KEY);
            if (body == null) {
                body = osmWayMapper.queryAllGeojson();
                cacheUtil.put(ALL_OSM_WAY_KEY, body, DAY);
            }
        } else {
            PGgeometry geometry = GeomUtil.genPolygon(xyarr, GeomUtil.MKT);
            body = osmWayMapper.queryByPolygonGeojson(geometry);
        }
        return body;
    }

    @Override
    public List<OSMWay> getWayByName(String name) {
        return osmWayMapper.queryByName(name);
    }

    @Override
    public List<OSMWay> getOsmLinksByArea(double[][] xyarr) {
        List<OSMWay> ways = osmWayMapper.queryByPolygon(GeomUtil.genPolygon(xyarr, GeomUtil.MKT));
        List<MatsimLink> links = matsimLinkMapper.queryByOrigids(ways.stream().map(OSMWay::getId).toList());
        Map<String, List<MatsimLink>> linkMap = links.stream().collect(Collectors.groupingBy(MatsimLink::getOrigid));
        ways.forEach(way -> {
            way.setLinks(linkMap.get(way.getId()));
        });
        return ways;
    }
}
