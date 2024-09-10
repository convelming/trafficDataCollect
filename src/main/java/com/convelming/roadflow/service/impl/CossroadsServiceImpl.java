package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson.JSON;
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
import com.convelming.roadflow.model.vo.CrossroadsVo;
import com.convelming.roadflow.model.vo.VoideFrameVo;
import com.convelming.roadflow.service.CossroadsService;
import com.convelming.roadflow.util.GeomUtil;
import com.convelming.roadflow.util.VideoUtil;
import com.convelming.roadflow.yolo.Yolo;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    public CrossroadsVo detail(Long cossroadsId) {
        Crossroads crossroads = mapper.selectById(cossroadsId);
        List<VoideFrameVo> frame = frame(cossroadsId);
        return new CrossroadsVo(crossroads, frame);
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
            return Collections.emptyList();
        }

        String toimage = video + "_0.jpg";
        File tif = new File(toimage);
        if (!tif.exists()) {
            VideoUtil.saveImage(video, toimage, VideoUtil.ImageType.JPG);
        }
        int[] wh = VideoUtil.widthight(toimage);
        VoideFrameVo vo = new VoideFrameVo();
        vo.setUrl("/file/download?url=" + toimage.replace(Constant.VIDEO_PATH, ""));
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
        cossroads.setMapInfo(bo.getMapInfo());

        PGgeometry polygon = GeomUtil.genPolygon(bo.getVertex(), GeomUtil.MKT);
        if (GeomUtil.getArea(polygon) > Constant.MAX_AREA) {
            throw new RuntimeException("范围过大，请缩小范围再提交");
        }
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
//        List<Link> ins = new ArrayList<>(), outs = new ArrayList<>(); // 进入、离开框选范围的link
        List<Link> intersectIns = new ArrayList<>(), intersectOuts = new ArrayList<>(); // 绘制线相交的in, out
        Collection<MatsimLink> lineIntersect = new ArrayList<>(); // 与绘制线相交的link
        for (CrossroadsController.LineBo line : bo.getLines()) {
            List<MatsimLink> intersectsLinks = linkMapper.selectIntersects(GeomUtil.genLine(new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy()), 3857));
            intersectsLinks.forEach(link -> link.setLineName(line.getLineName()));
            lineIntersect.addAll(intersectsLinks);
        }

        Map<String, MatsimLink> linkMap = lineIntersect.stream().collect(Collectors.toMap(MatsimLink::getId, x -> x));

//        miniNetWork.getLinks().forEach(((id, link) -> {
//            // 十字路所有进出口
//            if (!LineUtil.crossJudgment(bo.getVertex(), new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()})) { // 边界相交
////                    && !LineUtil.isPointOnline(new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()}, bo.getLines())) { // 绘制线相交
////            if (!LineUtil.isPointOnline(new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()}, bo.getLines())) { // 绘制线相交
//                return;
//            }
//            if (link.getFromNode().getInLinks().isEmpty()) {
//                ins.add(link);
//            }
//            if (link.getToNode().getOutLinks().isEmpty()) {
//                outs.add(link);
//            }
//        }));
        Table<String, Id<Link>, Coord> pointIntersect = HashBasedTable.create();
        for (MatsimLink link : lineIntersect) {
            Link l = miniNetWork.getLinks().get(Id.createLinkId(link.getId()));
            double todist = calcDistance3857(l.getToNode().getCoord(), centerCoord);
            double fromdist = calcDistance3857(l.getFromNode().getCoord(), centerCoord);

            // 计算交点
            Coord intersect = null;
            for (int i = 0; i < bo.getLines().size() && intersect == null; i++) {
                CrossroadsController.LineBo line = bo.getLines().get(i);
                intersect = doIntersect(new Coord[]{new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy())},
                        new Coord[]{l.getFromNode().getCoord(), l.getToNode().getCoord()});
            }

            if (todist > fromdist) {
                intersectOuts.add(l);
                pointIntersect.put("out", l.getId(), intersect);
            } else {
                intersectIns.add(l);
                pointIntersect.put("in", l.getId(), intersect);
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
                    cossroadsStats.setCrossroadsId(bo.getCossroadsId());
                    cossroadsStats.setOutLink(outLink.getId());
                    cossroadsStats.setInLink(inLink.getId());
//                    cossroadsStats.setPcuDetail(CrossroadsStats.DEFAULT_DETAIL); // 默认小中大客/货车
                    cossroadsStats.setResultId(inLink.getLineName() + outLink.getLineName());
                    cossroadsStats.setName(cossroadsStats.getResultId());
                    // 绘制线与link交点作为贝塞尔曲线的起点与终点
                    // in 为起点，out为终点
                    Coord startCoord = pointIntersect.get("in", in.getId());
                    if (startCoord != null) {
                        cossroadsStats.setStartPoint(JSON.toJSONString(new double[]{startCoord.getX(), startCoord.getY()}));
                    }
                    Coord endCoord = pointIntersect.get("out", out.getId());
                    if (endCoord != null) {
                        cossroadsStats.setEndPoint(JSON.toJSONString(new double[]{endCoord.getX(), endCoord.getY()}));
                    }
                    stats.add(cossroadsStats);
                }
            }
        }
        // 新增全部流量
        statsMapper.deleteByCrossroadsId(cossroads.getId()); // 调整位置之后把之前新增全部删除
        statsMapper.batchInsert(stats);
        boolean f = mapper.saveLines(cossroads) > 0;
        if (f && cossroads.getType().equals("2")) { // 只有视频录入才支持视频识别
            new Thread(() -> runVehicleCounts(cossroads.getId())).start();
        }
        return f;
    }

    @Override
    public List<CrossroadsStats> corssStatsTable(Long cossroadsId) {
        return statsMapper.selectByCrossroadsId(cossroadsId);
    }

    @Override
    public boolean deleteStats(Long crossroadStatsId) {
        return statsMapper.deleteById(crossroadStatsId);
    }

    @Override
    public boolean insertStats(CrossroadsStats stats) {

        if (mapper.selectById(stats.getCrossroadsId()) == null) {
            throw new RuntimeException("十字路不存在");
        }

        if (statsMapper.countCrossroadsInOrOutLink(stats.getCrossroadsId(), stats.getInLink(), stats.getOutLink()) <= 0) {
            throw new RuntimeException("所选link不在十字路范围内");
        }

        if (statsMapper.countCrossroadsInOutLink(stats.getCrossroadsId(), stats.getInLink(), stats.getOutLink()) > 0) {
            throw new RuntimeException("该进出link已存在");
        }

        // 计算pcu/h
//        if (stats.getPcuDetail() != null && !stats.getPcuDetail().isEmpty()) {
//            BigDecimal pcuh = new BigDecimal("0");
//            JSONArray pcus = JSONArray.parse(stats.getPcuDetail());
//            for (Object obj : pcus) {
//                JSONObject pcu = (JSONObject) obj;
//                Integer num = pcu.getInteger(CrossroadsStats.DETAIL_NUM);
//                BigDecimal ratio = pcu.getBigDecimal(CrossroadsStats.DETAIL_RATIO);
//                pcuh = pcuh.add(ratio.multiply(BigDecimal.valueOf(num)));
//            }
//            stats.setPcuH(pcuh.doubleValue());
//        }

        stats.setPcuH(calcPcu(stats));

        return statsMapper.insert(stats);
    }

    @Override
    public boolean updateStats(CrossroadsStats stats) {
        // 计算pcu/h
//        if (stats.getPcuDetail() != null && !stats.getPcuDetail().isEmpty()) {
//            BigDecimal pcuh = new BigDecimal("0");
//            JSONArray pcus = JSONArray.parse(stats.getPcuDetail());
//            for (Object obj : pcus) {
//                JSONObject pcu = (JSONObject) obj;
//                Integer num = pcu.getInteger(CrossroadsStats.DETAIL_NUM);
//                BigDecimal ratio = pcu.getBigDecimal(CrossroadsStats.DETAIL_RATIO);
//                pcuh = pcuh.add(ratio.multiply(BigDecimal.valueOf(num)));
//            }
//            stats.setPcuH(pcuh.doubleValue());
//        }

        stats.setPcuH(calcPcu(stats));

        return statsMapper.updateById(stats);
    }

    @Override
    public Map<String, Collection<String>> inoutlink(Long cossroadsId) {
        List<CrossroadsStats> stats = statsMapper.selectByCrossroadsId(cossroadsId);
        return new HashMap<>() {{
            put("inlink", stats.stream().map(CrossroadsStats::getInLink).collect(Collectors.toSet()));
            put("outlink", stats.stream().map(CrossroadsStats::getOutLink).collect(Collectors.toSet()));
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
                mapper.updateStatus(cossroadsId, 3); // 运行成功
                // 更新车辆数量
                Map<String, CrossroadsStats> maps = statsMapper.selectByCrossroadsId(cossroadsId).stream().collect(Collectors.toMap(CrossroadsStats::getResultId, x -> x, (a, b) -> a)); // todo 如果有重复去掉
                File results_csv = new File(Constant.DATA_PATH + "/data/" + cossroadsId + "/output_result/results.csv");
                try (RandomAccessFile raf = new RandomAccessFile(results_csv, "rw")) {
                    raf.readLine(); // 跳过标题行
                    String row;
                    while ((row = raf.readLine()) != null) {
                        String[] data = row.split(","); //id,car,bus,van,truck
                        CrossroadsStats stats = maps.get(data[0]);
                        if (stats == null) {
                            continue;
                        }
                        stats.setCar(Integer.parseInt(data[1]));
                        stats.setBus(Integer.parseInt(data[2]));
                        stats.setVan(Integer.parseInt(data[3]));
                        stats.setTruck(Integer.parseInt(data[4]));
                        stats.setPcuH(calcPcu(stats));
                        stats.setCount(stats.getCar() + stats.getBus() + stats.getVan() + stats.getTruck());
                    }
                    statsMapper.batchUpdate(maps.values());
                } catch (IOException e) {
                    log.error("读取results.csv失败");
                }
                log.info("运行视频识别成功");
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
        List<CrossroadsStats> stats = statsMapper.selectByCrossroadsId(cossroadsId);

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


    /**
     * 计算pcu
     *
     * @param stats  流量
     * @param second 时长
     * @return
     */
    private static double calcPcu(CrossroadsStats stats) {
        BigDecimal pcuh = new BigDecimal("0");
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getCar()));
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getBus()).multiply(BigDecimal.valueOf(2)));
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getVan()));
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getTruck()).multiply(BigDecimal.valueOf(2)));
//        return pcuh.multiply(BigDecimal.valueOf(3600)).divide(BigDecimal.valueOf(second), RoundingMode.HALF_UP).setScale(2, RoundingMode.DOWN).doubleValue();
        return pcuh.setScale(2, RoundingMode.DOWN).doubleValue();
    }

    private static Coord doIntersect(Coord[] line1, Coord[] line2) {
        double[] p1 = {line1[0].getX(), line1[0].getY()};
        double[] v1 = {line1[1].getX() - line1[0].getX(), line1[1].getY() - line1[0].getY()};
        double[] p2 = {line2[0].getX(), line2[0].getY()};
        double[] v2 = {line2[1].getX() - line2[0].getX(), line2[1].getY() - line2[0].getY()};
        double[] intersection = new double[2];
        // 计算两条线段的方向向量的叉积
        double crossProduct = v1[0] * v2[1] - v1[1] * v2[0];
        // 如果叉积接近于零，则两条线段平行，没有交点
        if (Math.abs(crossProduct) <= 1e-9) {
            intersection[0] = -1.0;
            intersection[1] = -1.0;
        }
        // 计算参数 t1 和 t2
        // 计算参数 t1 和 t2
        double t1 = ((p2[0] - p1[0]) * v2[1] - (p2[1] - p1[1]) * v2[0]) / crossProduct;
        // 计算交点坐标
        intersection[0] = p1[0] + t1 * v1[0];
        intersection[1] = p1[1] + t1 * v1[1];
        if (intersection[0] == -1.0 && intersection[1] == -1.0) {
            // 没有交点
            return null;
        } else {
            return new Coord(intersection);
        }
    }

}
