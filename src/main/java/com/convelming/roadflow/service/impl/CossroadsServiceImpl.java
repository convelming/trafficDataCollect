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

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;
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
        double[] center = GeomUtil.getCentroid(polygon);
        Coord centerCoord = new Coord(center);
        cossroads.setCenter(JSON.toJSONString(center));
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
        List<Link> ins = new ArrayList<>(), outs = new ArrayList<>(); // 进入、离开框选范围的link
        List<Link> intersectIns = new ArrayList<>(), intersectOuts = new ArrayList<>(); // 绘制线相交的in, out
        Collection<MatsimLink> lineIntersect = new ArrayList<>(); // 与绘制线相交的link
        for (CrossroadsController.LineBo line : bo.getLines()) {
            lineIntersect.addAll(linkMapper.selectIntersects(GeomUtil.genLine(new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy()), 3857)));
        }

        Map<String, MatsimLink> linkMap = links.stream().collect(Collectors.toMap(MatsimLink::getId, x -> x));

        miniNetWork.getLinks().forEach(((id, link) -> {
            // 十字路所有进出口
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
        for (MatsimLink link : lineIntersect) {
            Link l = miniNetWork.getLinks().get(Id.createLinkId(link.getId()));
            double todist = calcDistance3857(l.getToNode().getCoord(), centerCoord);
            double fromdist = calcDistance3857(l.getFromNode().getCoord(), centerCoord);
            if (todist > fromdist) {
                intersectOuts.add(l);
            } else {
                intersectIns.add(l);
            }
        }


        // 每个进口 * 出口构成一个流量数据 129171
        List<CrossroadsStats> stats = new ArrayList<>();
        for (Link in : intersectIns) {
            MatsimLink inLink = linkMap.get(in.getId().toString());
            for (Link out : intersectOuts) {
                // in -> out 能在mininetwork中走通才添加
                if (calcRouteAccessible(in, out, new Stack<>())) {
                    MatsimLink outLink = linkMap.get(out.getId().toString());
                    CrossroadsStats cossroadsStats = new CrossroadsStats();
                    cossroadsStats.setCossroadsId(bo.getCossroadsId());
                    cossroadsStats.setOutLink(outLink.getId());
                    cossroadsStats.setInLink(inLink.getId());
                    cossroadsStats.setPcuDetail(CrossroadsStats.DEFAULT_DETAIL); // 默认小中大客/货车
                    stats.add(cossroadsStats);
                }
            }
        }
        // 新增全部流量
        statsMapper.deleteByCossroadsId(cossroads.getId()); // 调整位置之后把之前新增全部删除
        statsMapper.batchInsert(stats);
        boolean f = mapper.saveLines(cossroads) > 0;
        if (f && cossroads.getType().equals("2")) { // 只有视频录入才支持视频识别
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
                log.info("运行视频失败成功");
            } else {
                mapper.updateStatus(cossroadsId, 4); // 运行失败
                log.error("运行视频识别失败");
            }
            return result;
        }

        return false;
    }

    @Override
    public void analyzeVideo(Long cossroadsId, HttpServletResponse response) {
        //

    }

    @Override
    public void statusFlowImage(Long cossroadsId, HttpServletResponse response) {
        int width = 600, height = 600;
//        Crossroads crossroads = mapper.selectById(cossroadsId);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        List<CrossroadsStats> stats = statsMapper.selectByCossroadsId(cossroadsId);

        for (CrossroadsStats stat : stats) {
            // 获取from to link 位置
            CubicCurve2D.Double bezierCurve = new CubicCurve2D.Double(
                    10.0, 10.0, // P0 x,y
                    100.0, 10.0, // P1 x,y
                    100.0, 100.0, // P2 x,y
                    10.0, 100.0); // P3 x,y);
            g2d.draw(bezierCurve);
        }

    }

    /**
     * 计算墨卡托坐标距离
     */
    private static double calcDistance3857(Coord coord1, Coord coord2) {
        return Math.sqrt(Math.pow(coord1.getX() - coord2.getX(), 2) + Math.pow(coord1.getY() - coord2.getY(), 2));
    }

    /**
     * 计算link直接是否连通
     *
     * @param start 起点
     * @param end   终点
     * @param stack 存储路径
     */
    private static boolean calcRouteAccessible(Link start, Link end, Stack<Id<Link>> stack) {
        stack.push(start.getId());
        if (start.getId().equals(end.getId())) {
            return true;
        }
        for (Map.Entry<Id<Link>, ? extends Link> entry : start.getToNode().getOutLinks().entrySet()) {
            Id<Link> id = entry.getKey();
            if (stack.contains(id) && !id.equals(start.getId())) {
                stack.pop();
                return false;
            }
            Link link = entry.getValue();
            if (calcRouteAccessible(link, end, stack)) {
                return true;
            }
            stack.pop();
        }
        return false;
    }

}
