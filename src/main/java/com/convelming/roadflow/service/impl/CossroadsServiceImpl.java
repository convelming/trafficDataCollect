package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.controller.CossroadsController;
import com.convelming.roadflow.mapper.CossroadsMapper;
import com.convelming.roadflow.mapper.CossroadsStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.mapper.MatsimNodeMapper;
import com.convelming.roadflow.model.Cossroads;
import com.convelming.roadflow.model.CossroadsStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.MatsimNode;
import com.convelming.roadflow.model.vo.VoideFrameVo;
import com.convelming.roadflow.service.CossroadsService;
import com.convelming.roadflow.util.GeomUtil;
import com.convelming.roadflow.util.LineUtil;
import com.convelming.roadflow.util.VideoUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import net.postgis.jdbc.PGgeometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CossroadsServiceImpl implements CossroadsService {

    @Resource
    private HttpServletRequest request;

    @Resource
    private CossroadsMapper mapper;

    @Resource
    private CossroadsStatsMapper statsMapper;

    @Resource
    private MatsimLinkMapper linkMapper;

    @Resource
    private MatsimNodeMapper nodeMapper;

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
        mapper.insert(cossroads);

        List<String> nodeIds = new ArrayList<>(links.stream().map(MatsimLink::getToNode).toList());
        nodeIds.addAll(links.stream().map(MatsimLink::getFromNode).toList());
        List<MatsimNode> nodes = nodeMapper.selectByIds(nodeIds);

        // 圈出来的路网
        Network miniNetWork = NetworkUtils.createNetwork();
        nodes.forEach(node -> {
            miniNetWork.addNode(NetworkUtils.createNode(Id.createNodeId(node.getId()), new Coord(node.getX(), node.getY())));
        });
        links.forEach(link -> {
            miniNetWork.addLink(NetworkUtils.createLink(
                    Id.createLinkId(link.getId()),
                    miniNetWork.getNodes().get(Id.createNodeId(link.getFromNode())),
                    miniNetWork.getNodes().get(Id.createNodeId(link.getToNode())),
                    miniNetWork, link.getLength(), link.getFreespeed(), link.getCapacity(), link.getLane()
            ));
        });
        // 十字路所有进出口
        List<Link> ins = new ArrayList<>(), outs = new ArrayList<>();
        miniNetWork.getLinks().forEach(((id, link) -> {
            if (!LineUtil.crossJudgment(vertex, new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()})) { // 边界相交
                return;
            }
            if (link.getFromNode().getInLinks().isEmpty()) {
                ins.add(link);
            }
            if (link.getToNode().getOutLinks().isEmpty()) {
                outs.add(link);
            }
        }));

        // 每个进口 * 出口构成一个流量数据
        Map<String, MatsimLink> linkMap = links.stream().collect(Collectors.toMap(MatsimLink::getId, x -> x));
        List<CossroadsStats> stats = new ArrayList<>();
        for (Link in : ins) {
            MatsimLink inLink = linkMap.get(in.getId().toString());
            for (Link out : outs) {
                MatsimLink outLink = linkMap.get(out.getId().toString());
                CossroadsStats cossroadsStats = new CossroadsStats();
                cossroadsStats.setCossroadsId(cossroads.getId());
                cossroadsStats.setOutLink(outLink.getId());
                cossroadsStats.setInLink(inLink.getId());
                cossroadsStats.setPcuDetail(CossroadsStats.DEFAULT_DETAIL); // 默认小中大客/货车
                stats.add(cossroadsStats);
            }
        }
        // 新增全部流量
        statsMapper.batchInsert(stats);

        return true;
    }

    @Override
    public List<VoideFrameVo> frame(Long id) {
        Cossroads cossroads = mapper.selectById(id);
        if (cossroads == null) {
            return Collections.emptyList();
        }
        String video = Constant.VIDEO_PATH + cossroads.getVideo();
        File vf = new File(video);
        if (!vf.exists()) {
            throw new RuntimeException("视频文件不存在");
        }

        String toimage = video + "_0.jpg";
        File tif = new File(toimage);
        if (!tif.exists()) {
            VideoUtil.saveImage(video, toimage, VideoUtil.ImageType.JPG);
        }
        int[] wh = VideoUtil.widthight(toimage);
        VoideFrameVo vo = new VoideFrameVo();
        vo.setUrl("/file/download/" + toimage.replace(Constant.VIDEO_PATH, ""));
        vo.setName(toimage.substring(toimage.lastIndexOf("/") + 1));
        vo.setWidth(wh[0]);
        vo.setHeight(wh[1]);
        return List.of(vo);
    }

    @Override
    public boolean saveline(List<CossroadsController.LineBo> lines) {
        if (lines.isEmpty()) {
            return false;
        }
        Long id = lines.get(0).getCossroadsId();
        Cossroads cossroads = mapper.selectById(id);
        if (cossroads.getLines() != null) {
            cossroads.setVersion(cossroads.getVersion() + 1);
        }
        cossroads.setUpdateTime(new Date());
        cossroads.setLines(JSON.toJSONString(lines));
        return mapper.saveLines(cossroads) > 0;
    }


}
