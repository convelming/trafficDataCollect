package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.CrossroadsController;
import com.convelming.roadflow.mapper.CrossroadsMapper;
import com.convelming.roadflow.mapper.CrossroadsStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.mapper.MatsimNodeMapper;
import com.convelming.roadflow.model.Crossroads;
import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.MatsimNode;
import com.convelming.roadflow.model.vo.VoideFrameVo;
import com.convelming.roadflow.service.CossroadsService;
import com.convelming.roadflow.util.GeomUtil;
import com.convelming.roadflow.util.LineUtil;
import com.convelming.roadflow.util.VideoUtil;
import com.convelming.roadflow.yolo.Yolo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CossroadsServiceImpl implements CossroadsService {

    @Resource
    private HttpServletRequest request;

    @Resource
    private CrossroadsMapper mapper;

    @Resource
    private CrossroadsStatsMapper statsMapper;

    @Resource
    private MatsimLinkMapper linkMapper;

    @Resource
    private MatsimNodeMapper nodeMapper;

    @Override
    public Page<Crossroads> list(Page<Crossroads> page) {
        return mapper.list(page);
    }

    @Override
    public Crossroads insert(CrossroadsController.CossroadsBo bo) {

        Crossroads crossroads = new Crossroads();
        crossroads.setVideo(bo.getVideo());
        crossroads.setType(bo.getType());
        crossroads.setRemark(bo.getRemark());
        crossroads.setVideoType(bo.getVideoType());
        crossroads.setIpAddr(request.getRemoteAddr());

        crossroads.setBeginTime(bo.getBeginTime());
        crossroads.setEndTime(bo.getEndTime());
//        PGgeometry polygon = GeomUtil.genPolygon(vertex, GeomUtil.MKT);
//        List<MatsimLink> links = linkMapper.selectIntersects(polygon);
//        crossroads.setVertex(JSON.toJSONString(vertex));
        crossroads.setCenter(JSON.toJSONString(bo.getCenter()));
//        crossroads.setInLinkId(JSON.toJSONString(links.stream().map(MatsimLink::getId).toList()));
        mapper.insert(crossroads);
        return crossroads;
    }

    @Override
    public List<VoideFrameVo> frame(Long cossroadsId) {
        Crossroads crossroads = mapper.selectById(cossroadsId);
        if (crossroads == null) {
            return Collections.emptyList();
        }
        String video = Constant.VIDEO_PATH + crossroads.getVideo();
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
        vo.setUrl("/file/download?url=" + toimage.replace(Constant.DATA_PATH, ""));
        vo.setName(toimage.substring(toimage.lastIndexOf("/") + 1));
        vo.setWidth(wh[0]);
        vo.setHeight(wh[1]);
        vo.setCenter(JSON.parseArray(crossroads.getCenter(), BigDecimal.class).stream().mapToDouble(BigDecimal::doubleValue).toArray());
        return List.of(vo);
    }

    @Override
    public boolean saveline(CrossroadsController.CossroadsLineBo bo) {
        Crossroads cossroads = mapper.selectById(bo.getCossroadsId());
        if (cossroads.getLines() != null) {
            cossroads.setVersion(cossroads.getVersion() + 1);
        }
        cossroads.setStatus(1); // 设置状态已绘制检测线
        cossroads.setUpdateTime(new Date());
        cossroads.setVertex(JSON.toJSONString(bo.getVertex()));
        cossroads.setLines(JSON.toJSONString(bo.getLines()));

        PGgeometry polygon = GeomUtil.genPolygon(bo.getVertex(), GeomUtil.MKT);
        List<MatsimLink> links = linkMapper.selectIntersects(polygon);
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
            if (!LineUtil.crossJudgment(bo.getVertex(), new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()})) { // 边界相交
//                    && !LineUtil.isPointOnline(new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()}, bo.getLines())) { // 绘制线相交
//            if (!LineUtil.isPointOnline(new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()}, bo.getLines())) { // 绘制线相交
                return;
            }
            if (link.getFromNode().getInLinks().isEmpty()) {
                ins.add(link);
            }
            if (link.getToNode().getOutLinks().isEmpty()) {
                outs.add(link);
            }
        }));

        // todo link匹配绘制线

        // 每个进口 * 出口构成一个流量数据
        Map<String, MatsimLink> linkMap = links.stream().collect(Collectors.toMap(MatsimLink::getId, x -> x));
        List<CrossroadsStats> stats = new ArrayList<>();
        for (Link in : ins) {
            MatsimLink inLink = linkMap.get(in.getId().toString());
            for (Link out : outs) {
                MatsimLink outLink = linkMap.get(out.getId().toString());
                CrossroadsStats cossroadsStats = new CrossroadsStats();
                cossroadsStats.setCossroadsId(bo.getCossroadsId());
                cossroadsStats.setOutLink(outLink.getId());
                cossroadsStats.setInLink(inLink.getId());
                cossroadsStats.setPcuDetail(CrossroadsStats.DEFAULT_DETAIL); // 默认小中大客/货车
                stats.add(cossroadsStats);
            }
        }
        // 新增全部流量
        statsMapper.deleteByCossroadsId(cossroads.getId()); // 调整位置之后把之前新增全部删除
        statsMapper.batchInsert(stats);
        boolean f = mapper.saveLines(cossroads) > 0;
        if (f) {
            new Thread(() -> runVehicleCounts(cossroads.getId())).start();
        }
        return f;
    }

    @Override
    public List<CrossroadsStats> corssStatsTable(Long cossroadsId) {
        return statsMapper.selectByCossroadsId(cossroadsId);
    }

    @Override
    public boolean deleteStats(Long crossroadStatsId) {
        return statsMapper.deleteByCossroadsId(crossroadStatsId);
    }

    @Override
    public boolean insertStats(CrossroadsStats stats) {

        if (mapper.selectById(stats.getCossroadsId()) == null) {
            throw new RuntimeException("十字路不存在");
        }

        if (statsMapper.countCossroadsInOrOutLink(stats.getCossroadsId(), stats.getInLink(), stats.getOutLink()) <= 0) {
            throw new RuntimeException("所选link不在十字路范围内");
        }

        if (statsMapper.countCossroadsInOutLink(stats.getCossroadsId(), stats.getInLink(), stats.getOutLink()) > 0) {
            throw new RuntimeException("该进出link已存在");
        }

        // 计算pcu/h
        if (stats.getPcuDetail() != null && !stats.getPcuDetail().isEmpty()) {
            BigDecimal pcuh = new BigDecimal("0");
            JSONArray pcus = JSONArray.parse(stats.getPcuDetail());
            for (Object obj : pcus) {
                JSONObject pcu = (JSONObject) obj;
                Integer num = pcu.getInteger(CrossroadsStats.DETAIL_NUM);
                BigDecimal ratio = pcu.getBigDecimal(CrossroadsStats.DETAIL_RATIO);
                pcuh = pcuh.add(ratio.multiply(BigDecimal.valueOf(num)));
            }
            stats.setPcuH(pcuh.doubleValue());
        }
        return statsMapper.insert(stats);
    }

    @Override
    public boolean updateStats(CrossroadsStats stats) {
        // 计算pcu/h
        if (stats.getPcuDetail() != null && !stats.getPcuDetail().isEmpty()) {
            BigDecimal pcuh = new BigDecimal("0");
            JSONArray pcus = JSONArray.parse(stats.getPcuDetail());
            for (Object obj : pcus) {
                JSONObject pcu = (JSONObject) obj;
                Integer num = pcu.getInteger(CrossroadsStats.DETAIL_NUM);
                BigDecimal ratio = pcu.getBigDecimal(CrossroadsStats.DETAIL_RATIO);
                pcuh = pcuh.add(ratio.multiply(BigDecimal.valueOf(num)));
            }
            stats.setPcuH(pcuh.doubleValue());
        }
        return statsMapper.updateById(stats);
    }

    @Override
    public Map<String, List<String>> inoutlink(Long cossroadsId) {
        List<CrossroadsStats> stats = statsMapper.selectByCossroadsId(cossroadsId);
        return new HashMap<>() {{
            put("inlink", stats.stream().map(CrossroadsStats::getInLink).toList());
            put("outlink", stats.stream().map(CrossroadsStats::getOutLink).toList());
        }};
    }

    @Override
    public boolean runVehicleCounts(Long cossroadsId) {
        Crossroads crossroads = mapper.selectById(cossroadsId);

        if (crossroads.getVideo() == null) {
            throw new RuntimeException("运行失败，未上传视频");
        }

        if (crossroads.getStatus() == 0) {
            throw new RuntimeException("运行失败，未绘制检测线");
        }

        if (crossroads.getStatus() == 2) {
            throw new RuntimeException("正在运行，不能重复运行");
        }

        if (crossroads.getStatus() == 3) {
            throw new RuntimeException("已运行，不能重复运行");
        }

        if (crossroads.getStatus() == 1 || crossroads.getStatus() == 4) {
            mapper.updateStatus(cossroadsId, 2);
            boolean result = Yolo.run(crossroads);
            if (result) { // 成功运行
                // 后续操作
                mapper.updateStatus(cossroadsId, 3); // 运行成功
            } else {
                mapper.updateStatus(cossroadsId, 4); // 运行失败
            }
            return result;
        }

        return false;
    }

    @Override
    public void analyzeVideo(Long cossroadsId, HttpServletResponse response) {
        //

    }


}
