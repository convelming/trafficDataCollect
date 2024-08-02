package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.convelming.roadflow.controller.CossroadsController;
import com.convelming.roadflow.mapper.CossroadsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.model.Cossroads;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.service.CossroadsService;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import net.postgis.jdbc.PGgeometry;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CossroadsServiceImpl implements CossroadsService {

    @Resource
    private HttpServletRequest request;

    @Resource
    private CossroadsMapper mapper;

    @Resource
    private MatsimLinkMapper linkMapper;

    @Override
    public boolean insert(CossroadsController.CossroadsBo bo, double[][] vertex) {

        Cossroads cossroads = new Cossroads();
        cossroads.setVideo(bo.getVideo());
        cossroads.setType(bo.getType());
        cossroads.setRemark(bo.getRemark());
        cossroads.setIpAddr(request.getRemoteAddr());


        cossroads.setBeginTime(bo.getBeginTime());
        cossroads.setEndTime(bo.getEndTime());

        PGgeometry polygon = GeomUtil.genPolygon(vertex, GeomUtil.MKT);
        List<MatsimLink> links = linkMapper.selectIntersects(polygon);
        cossroads.setVertex(JSON.toJSONString(vertex));
        cossroads.setCenter(JSON.toJSONString(GeomUtil.getCentroid(polygon)));
        cossroads.setInLinkId(JSON.toJSONString(links.stream().map(MatsimLink::getId).toList()));

        return mapper.insert(cossroads);
    }
}
